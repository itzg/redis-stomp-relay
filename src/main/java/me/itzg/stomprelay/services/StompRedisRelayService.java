package me.itzg.stomprelay.services;

import me.itzg.stomprelay.config.StompRedisRelayProperties;
import me.itzg.stomprelay.handlers.StompFrameHandler;
import me.itzg.stomprelay.handlers.StompFrameHandlerFactory;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.stomp.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.EnumMap;
import java.util.List;

/**
 * @author Geoff Bourne
 */
@Service
public class StompRedisRelayService {
    private static final Logger LOGGER = LoggerFactory.getLogger(StompRedisRelayService.class);

    private final StompRedisRelayProperties properties;
    private final SubscriptionManagement subscriptionManagement;
    private final EnumMap<StompCommand, StompFrameHandlerFactory> handlerFactories;
    private NioEventLoopGroup bossGroup;
    private NioEventLoopGroup workerGroup;

    @Autowired
    public StompRedisRelayService(StompRedisRelayProperties properties,
                                  List<StompFrameHandlerFactory> handlerFactories,
                                  SubscriptionManagement subscriptionManagement) {
        this.properties = properties;
        this.subscriptionManagement = subscriptionManagement;

        this.handlerFactories = new EnumMap<>(StompCommand.class);
        for (StompFrameHandlerFactory handlerFactory : handlerFactories) {
            LOGGER.debug("Registering handler factory {} for {}", handlerFactory, handlerFactory.getCommand());
            this.handlerFactories.put(handlerFactory.getCommand(), handlerFactory);
        }
    }

    @PostConstruct
    public void start() {
        try {
            bossGroup = new NioEventLoopGroup();
            workerGroup = new NioEventLoopGroup();
            LOGGER.info("Starting...");
            new ServerBootstrap()
                    .group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel channel) throws Exception {
                            final ChannelPipeline pipeline = channel.pipeline();

                            pipeline.addLast(new StompSubframeDecoder());
                            pipeline.addLast(new StompSubframeEncoder());

                            pipeline.addLast(new StompSubframeAggregator(properties.getMaxContentLength()));

                            pipeline.addLast(new InboundStompFrameHandler());

                        }
                    })
                    .bind(properties.getPort())
                    .sync();

            LOGGER.info("STOMP relay service started on port {}", properties.getPort());

        } catch (InterruptedException e) {
            throw new IllegalStateException("Failed to startup server channel", e);
        }
    }

    @PreDestroy
    public void stop() {
        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
        bossGroup = null;
        workerGroup = null;
    }

    private class InboundStompFrameHandler extends SimpleChannelInboundHandler<StompFrame> {

        @Override
        protected void channelRead0(ChannelHandlerContext context, StompFrame stompFrame) throws Exception {
            final StompCommand command = stompFrame.command();
            LOGGER.trace("Processing incoming STOMP {} frame: {}", command, stompFrame);
            final StompHeaders headers = stompFrame.headers();

            final StompFrameHandlerFactory handlerFactory = handlerFactories.get(command);
            if (handlerFactory == null) {
                LOGGER.warn("Received an unsupported command {}", command);
                DefaultStompFrame response = new DefaultStompFrame(StompCommand.ERROR);
                context.writeAndFlush(response);
                context.close();
                return;
            }

            final StompFrameHandler stompFrameHandler = handlerFactory.create(context, headers);
            stompFrameHandler.invoke();

            final DefaultStompFrame response = stompFrameHandler.getResponse();
            if (response != null) {
                LOGGER.trace("Responding with STOMP frame: {}", response);
                context.writeAndFlush(response);

            }

            if (stompFrameHandler.isCloseAfterResponse()) {
                context.close();
                subscriptionManagement.unsubscribeAllForContext(context);
            }
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext context, Throwable cause) throws Exception {
            LOGGER.warn("Exception in channel", cause);
            subscriptionManagement.unsubscribeAllForContext(context);
            context.close();
        }
    }
}

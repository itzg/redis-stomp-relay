package me.itzg.stomprelay.handlers;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.stomp.DefaultStompFrame;
import io.netty.handler.codec.stomp.StompCommand;
import io.netty.handler.codec.stomp.StompHeaders;
import me.itzg.stomprelay.services.SubscriptionManagement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Geoff Bourne
 */
@Component
public class DisconnectHandlerFactory implements StompFrameHandlerFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(DisconnectHandlerFactory.class);
    private final SubscriptionManagement subscriptionManagement;

    @Autowired
    public DisconnectHandlerFactory(SubscriptionManagement subscriptionManagement) {
        this.subscriptionManagement = subscriptionManagement;
    }

    @Override
    public StompCommand getCommand() {
        return StompCommand.DISCONNECT;
    }

    @Override
    public StompFrameHandler create(ChannelHandlerContext context, StompHeaders headers, ByteBuf content) {
        return new Handler(context, headers, content);
    }

    private class Handler extends AbstractStompFrameHandler {

        public Handler(ChannelHandlerContext context, StompHeaders headers, ByteBuf content) {
            super(context, headers, content);
        }

        @Override
        public StompFrameHandler invoke() {

            final String receipt = headers.getAsString(StompHeaders.RECEIPT);
            LOGGER.debug("Processing disconnect from {}", context.channel().remoteAddress());

            if (receipt != null) {
                // since we relay immediately, we can ack the receipt of whatever the client asks
                response = new DefaultStompFrame(StompCommand.RECEIPT);
                response.headers().set(StompHeaders.RECEIPT_ID, receipt);
            }
            closeAfterResponse = true;

            return this;
        }
    }
}

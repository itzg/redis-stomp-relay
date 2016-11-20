package me.itzg.stomprelay.handlers;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.stomp.DefaultStompFrame;
import io.netty.handler.codec.stomp.StompCommand;
import io.netty.handler.codec.stomp.StompHeaders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.nio.charset.Charset;

/**
 * @author Geoff Bourne
 */
@Component
public class ConnectHandlerFactory implements StompFrameHandlerFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConnectHandlerFactory.class);

    @Override
    public StompCommand getCommand() {
        return StompCommand.CONNECT;
    }

    @Override
    public StompFrameHandler create(ChannelHandlerContext context, StompHeaders headers) {
        return new Handler(context, headers);
    }

    private class Handler extends AbstractStompFrameHandler {

        public Handler(ChannelHandlerContext context, StompHeaders headers) {
            super(context, headers);
        }

        @Override
        public StompFrameHandler invoke() {
            final String[] acceptedVersions = headers.getAsString(StompHeaders.ACCEPT_VERSION).split(",");

            if (arrayContains(acceptedVersions, "1.1")) {
                response = new DefaultStompFrame(StompCommand.CONNECTED);
                response.headers().set(StompHeaders.VERSION, "1.1");
                // purposely don't include heart-beat header since we don't implement it yet
                response.headers().set(StompHeaders.HEART_BEAT, "0,0");
                LOGGER.info("Accepting STOMP connection from {}", context.channel().remoteAddress());
            }
            else {
                LOGGER.warn("Received unsupported STOMP versions in connect: {}", acceptedVersions);
                buildErrorResponse("Unsupported version");
            }

            return this;
        }
    }
}

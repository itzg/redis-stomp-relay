package me.itzg.stomprelay.handlers;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.stomp.StompCommand;
import io.netty.handler.codec.stomp.StompHeaders;
import me.itzg.stomprelay.config.StompRedisRelayProperties;
import me.itzg.stomprelay.services.SubscriptionManagement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Geoff Bourne
 */
@Component
public class UnsubscribeHandlerFactory implements StompFrameHandlerFactory {
    private final SubscriptionManagement subscriptionManagement;

    @Autowired
    public UnsubscribeHandlerFactory(SubscriptionManagement subscriptionManagement) {
        this.subscriptionManagement = subscriptionManagement;
    }

    @Override
    public StompCommand getCommand() {
        return StompCommand.UNSUBSCRIBE;
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
        public Handler invoke() {
            final String subscriptionId = headers.getAsString(StompHeaders.ID);

            try {
                subscriptionManagement.unsubscribe(context, subscriptionId);
            } catch (IllegalArgumentException e) {
                buildErrorResponse(e.getMessage());
            }

            return this;
        }
    }
}
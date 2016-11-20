package me.itzg.stomprelay.handlers;

import me.itzg.stomprelay.config.StompRedisRelayProperties;
import me.itzg.stomprelay.services.SubscriptionManagement;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.stomp.DefaultStompFrame;
import io.netty.handler.codec.stomp.StompCommand;
import io.netty.handler.codec.stomp.StompHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.charset.Charset;

/**
 * @author Geoff Bourne
 */
@Component
public class SubscribeHandlerFactory implements StompFrameHandlerFactory {
    private final StompRedisRelayProperties properties;
    private final SubscriptionManagement subscriptionManagement;

    @Autowired
    public SubscribeHandlerFactory(StompRedisRelayProperties properties,
                                   SubscriptionManagement subscriptionManagement) {
        this.properties = properties;
        this.subscriptionManagement = subscriptionManagement;
    }

    @Override
    public StompCommand getCommand() {
        return StompCommand.SUBSCRIBE;
    }

    @Override
    public StompFrameHandler create(ChannelHandlerContext context, StompHeaders headers) {
        return new SubscribeHandler(context, headers);
    }

    private class SubscribeHandler extends AbstractStompFrameHandler {

        public SubscribeHandler(ChannelHandlerContext context, StompHeaders headers) {
            super(context, headers);
        }

        @Override
        public SubscribeHandler invoke() {
            final String subscriptionId = headers.getAsString(StompHeaders.ID);
            final String destination = headers.getAsString(StompHeaders.DESTINATION);
            if (!destination.startsWith(properties.getChannelPrefix())) {
                buildErrorResponse("Incorrect subscription prefix");
            }

            subscriptionManagement.subscribe(context,
                    destination.substring(properties.getChannelPrefix().length()),
                    subscriptionId);
            return this;
        }

    }
}
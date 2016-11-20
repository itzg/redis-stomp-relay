package me.itzg.stomprelay.services;

import me.itzg.stomprelay.config.StompRedisRelayProperties;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.stomp.DefaultStompFrame;
import io.netty.handler.codec.stomp.StompCommand;
import io.netty.handler.codec.stomp.StompHeaders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.Topic;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Geoff Bourne
 */
@Service
public class SubscriptionManagement {
    private static final Logger LOGGER = LoggerFactory.getLogger(SubscriptionManagement.class);

    private final StompRedisRelayProperties properties;
    private final RedisMessageListenerContainer redisMessageListenerContainer;

    @Autowired
    public SubscriptionManagement(StompRedisRelayProperties properties,
                                  RedisMessageListenerContainer redisMessageListenerContainer) {
        this.properties = properties;
        this.redisMessageListenerContainer = redisMessageListenerContainer;
    }

    public void subscribe(ChannelHandlerContext context, String channel, String subscriptionId) {
        final PerSubscriptionListener listener = new PerSubscriptionListener(context, subscriptionId);

        Topic topic = new ChannelTopic(properties.getRedisChannelPrefix() + channel);
        LOGGER.debug("Subscribing client address={} subscription={} mapping channel={} onto topic={}",
                context.channel().remoteAddress(), subscriptionId, channel, topic);

        redisMessageListenerContainer.addMessageListener(listener, topic);
    }

    private class PerSubscriptionListener implements MessageListener {
        private final ChannelHandlerContext context;
        private final String subscriptionId;
        private final AtomicInteger messageId = new AtomicInteger(1);

        public PerSubscriptionListener(ChannelHandlerContext context, String subscriptionId) {
            this.context = context;
            this.subscriptionId = subscriptionId;
        }

        @Override
        public void onMessage(Message message, byte[] bytes) {
            LOGGER.trace("Received Redis channel message {}", message);
            final String channel = new String(message.getChannel())
                    .substring(properties.getRedisChannelPrefix().length());
            final byte[] body = message.getBody();

            final DefaultStompFrame messageFrame = new DefaultStompFrame(StompCommand.MESSAGE);
            final StompHeaders headersOut = messageFrame.headers();
            headersOut.add(StompHeaders.SUBSCRIPTION, subscriptionId);
            headersOut.add(StompHeaders.MESSAGE_ID, Integer.toString(messageId.getAndIncrement()));
            headersOut.add(StompHeaders.DESTINATION, properties.getChannelPrefix() + channel);
            headersOut.add(StompHeaders.CONTENT_TYPE, properties.getContentType());
            headersOut.addInt(StompHeaders.CONTENT_LENGTH, body.length);
            messageFrame.content().writeBytes(body);

            context.writeAndFlush(messageFrame);
        }
    }
}

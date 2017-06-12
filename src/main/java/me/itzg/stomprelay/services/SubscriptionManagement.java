package me.itzg.stomprelay.services;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.stomp.DefaultStompFrame;
import io.netty.handler.codec.stomp.StompCommand;
import io.netty.handler.codec.stomp.StompHeaders;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import me.itzg.stomprelay.config.StompRedisRelayProperties;
import me.itzg.stomprelay.helpers.Listener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.Topic;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.stereotype.Service;

/**
 * @author Geoff Bourne
 */
@Service
public class SubscriptionManagement {

    private static final Logger LOGGER = LoggerFactory.getLogger(SubscriptionManagement.class);
    private final StompRedisRelayProperties properties;
    private final RedisMessageListenerContainer redisMessageListenerContainer;

    private ConcurrentHashMap<String/*listnerUniqueId*/, PerSubscriptionListenerAdapter> subscriptions =
            new ConcurrentHashMap<>();

    @Autowired
    public SubscriptionManagement(
        StompRedisRelayProperties properties,
        RedisMessageListenerContainer redisMessageListenerContainer) {

        this.properties = properties;
        this.redisMessageListenerContainer = redisMessageListenerContainer;
    }

    public void subscribe(ChannelHandlerContext context, String channel, String subId) {

        // dont want to trust the client to guive me a unique session id
        // so, going to add some salt to the session id and try to mitigate the chances for the client miss behaving
        String listenerId = Listener.getListenerUniqueId(context, subId);

        if (!subscriptions.containsKey(listenerId)) {
            Topic topic = new ChannelTopic(properties.getRedisChannelPrefix() + channel);
            LOGGER.debug("Subscribing client address={} subscription={} mapping channel={} onto topic={}",
                context.channel().remoteAddress(), subId, channel, topic);

            PerSubscriptionListenerAdapter listener = new PerSubscriptionListenerAdapter(context, subId, listenerId);
            redisMessageListenerContainer.addMessageListener(listener, topic);
            subscriptions.put(listenerId, listener);
        } else {

            LOGGER.warn(
                "Listner [{}] already exists. Maybe clients are not sending unique session ids.",
                listenerId);
        }
    }

    public void unsubscribe(ChannelHandlerContext context, String listnerUniqueId) {

        PerSubscriptionListenerAdapter listener = subscriptions.remove(listnerUniqueId);

        if (listener != null) {
            redisMessageListenerContainer.removeMessageListener(listener);
            LOGGER.debug("Unsubscribed subscription ID={} from context={}", listnerUniqueId, context);
        } else {
            throw new IllegalArgumentException(String.format("Unknown subscription ID [%s]", listnerUniqueId));
        }
    }

    public void unsubscribeAllForContext(ChannelHandlerContext context) {
        subscriptions.entrySet().stream()
                .filter(e -> e.getValue().context.channel().equals(context.channel()))
                .forEach(e -> unsubscribe(context, e.getKey()));
    }

    private class PerSubscriptionListenerAdapter extends MessageListenerAdapter {
        private final ChannelHandlerContext context;
        private final AtomicInteger messageId = new AtomicInteger(1);

        public PerSubscriptionListenerAdapter(ChannelHandlerContext context, String subId, String listenerId) {

            this.context = context;
            this.setDelegate(new MessageListener() {
                @Override
                public void onMessage(Message message, byte[] pattern) {
                    LOGGER.trace("Subscription [{}] received redis channel message [{}]", listenerId, message);
                    final String channel = new String(message.getChannel())
                        .substring(properties.getRedisChannelPrefix().length());
                    final byte[] body = message.getBody();

                    final DefaultStompFrame messageFrame = new DefaultStompFrame(StompCommand.MESSAGE);
                    messageFrame.headers().add(StompHeaders.SUBSCRIPTION, subId);
                    messageFrame.headers().add(StompHeaders.MESSAGE_ID, Integer.toString(messageId.getAndIncrement()));
                    messageFrame.headers().add(StompHeaders.DESTINATION, properties.getChannelPrefix() + channel);
                    messageFrame.headers().add(StompHeaders.CONTENT_TYPE, properties.getContentType());
                    messageFrame.headers().addInt(StompHeaders.CONTENT_LENGTH, body.length);
                    messageFrame.content().writeBytes(body);

                    context.writeAndFlush(messageFrame);
                }
            });
        }
    }
}

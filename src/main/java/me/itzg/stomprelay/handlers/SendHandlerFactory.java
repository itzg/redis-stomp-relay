package me.itzg.stomprelay.handlers;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.stomp.StompCommand;
import io.netty.handler.codec.stomp.StompHeaders;
import java.nio.charset.Charset;
import me.itzg.stomprelay.config.StompRedisRelayProperties;
import me.itzg.stomprelay.services.SubscriptionManagement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

/**
 * @author @albertocsm
 */
@Component
public class SendHandlerFactory implements StompFrameHandlerFactory {

  private static final Logger LOGGER = LoggerFactory.getLogger(SendHandlerFactory.class);
  private RedisTemplate<String, String> redisTemplate;
  private final StompRedisRelayProperties properties;
  private final SubscriptionManagement subscriptionManagement;

  @Autowired
  public SendHandlerFactory(
      StompRedisRelayProperties properties,
      SubscriptionManagement subscriptionManagement,
      RedisTemplate<String, String> redisTemplate) {

    this.redisTemplate = redisTemplate;
    this.properties = properties;
    this.subscriptionManagement = subscriptionManagement;
  }

  @Override
  public StompCommand getCommand() {
    return StompCommand.SEND;
  }

  @Override
  public StompFrameHandler create(
      ChannelHandlerContext context,
      StompHeaders headers,
      ByteBuf content) {
    return new SendHandler(context, headers, content);
  }

  private class SendHandler extends AbstractStompFrameHandler {

    public SendHandler(
        ChannelHandlerContext context,
        StompHeaders headers,
        ByteBuf content) {
      super(context, headers, content);
    }

    @Override
    public SendHandler invoke() {

      final String destination = headers.getAsString(StompHeaders.DESTINATION);
      String channel = destination.substring(properties.getChannelPrefix().length());

      if (!destination.startsWith(properties.getChannelPrefix())) {
        buildErrorResponse("Incorrect subscription prefix");
      }

      redisTemplate.convertAndSend(
          properties.getRedisChannelPrefix() + channel,
          content.toString(Charset.defaultCharset())
      );

      return this;
    }

  }
}
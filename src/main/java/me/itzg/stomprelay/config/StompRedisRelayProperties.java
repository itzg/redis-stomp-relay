package me.itzg.stomprelay.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author Geoff Bourne
 */
@Component
@ConfigurationProperties("stomp-redis-relay")
@Data
public class StompRedisRelayProperties {
    int port = 61613;

    int maxContentLength = 1024 * 64;

    String channelPrefix = "/channel/";

    String redisChannelPrefix = "stomp:";

    /**
     * All message bodies published over Redis will be required to be this configured type.
     */
    String contentType = "application/json";
}

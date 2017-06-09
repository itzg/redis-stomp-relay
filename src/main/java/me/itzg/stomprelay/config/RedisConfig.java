package me.itzg.stomprelay.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * @author Geoff Bourne
 */
@Configuration
public class RedisConfig {

    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(
            // provided by Spring Boot
            @SuppressWarnings("SpringJavaAutowiringInspection") RedisConnectionFactory redisConnectionFactory
    ) {
        final RedisMessageListenerContainer bean = new RedisMessageListenerContainer();
        bean.setConnectionFactory(redisConnectionFactory);

        return bean;
    }

    @Bean
    StringRedisSerializer stringRedisSerializer() {
        return new StringRedisSerializer();
    }
}

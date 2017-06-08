package me.itzg.stomprelay.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JSR310Module;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;

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
    public Jackson2JsonRedisSerializer<Object> messageRedisSerializer() {

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JSR310Module());

        Jackson2JsonRedisSerializer<Object> serializer = new Jackson2JsonRedisSerializer(Object.class);
        serializer.setObjectMapper(objectMapper);
        return serializer;
    }

    @Bean
    public RedisTemplate<String, Object> objectRedisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<String, Object>();
        redisTemplate.setConnectionFactory(connectionFactory);
        redisTemplate.setValueSerializer(messageRedisSerializer());
        return redisTemplate;
    }
}

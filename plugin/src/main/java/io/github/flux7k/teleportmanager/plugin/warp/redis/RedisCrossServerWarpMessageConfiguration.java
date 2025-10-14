package io.github.flux7k.teleportmanager.plugin.warp.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.flux7k.teleportmanager.plugin.warp.CrossServerWarpMessage;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;

@Configuration
public class RedisCrossServerWarpMessageConfiguration {

    @Bean
    public ReactiveRedisTemplate<String, CrossServerWarpMessage> crossServerWarpMessageReactiveRedisTemplate(
        ReactiveRedisConnectionFactory connectionFactory,
        ObjectMapper objectMapper
    ) {
        RedisSerializer<String> keySerializer = RedisSerializer.string();
        RedisSerializer<CrossServerWarpMessage> valueSerializer = new Jackson2JsonRedisSerializer<>(objectMapper, CrossServerWarpMessage.class);
        RedisSerializationContext<String, CrossServerWarpMessage> serializationContext = RedisSerializationContext.<String, CrossServerWarpMessage>newSerializationContext()
            .key(keySerializer)
            .value(valueSerializer)
            .hashKey(keySerializer)
            .hashValue(valueSerializer)
            .build();
        return new ReactiveRedisTemplate<>(connectionFactory, serializationContext);
    }

}

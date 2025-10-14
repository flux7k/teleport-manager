package io.github.flux7k.teleportmanager.core.warp.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.flux7k.teleportmanager.core.warp.WarpPoint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;

@Configuration
public class RedisWarpPointConfiguration {

    @Bean
    public ReactiveRedisTemplate<String, WarpPoint> warpPointReactiveRedisTemplate(
        ReactiveRedisConnectionFactory connectionFactory,
        ObjectMapper objectMapper
    ) {
        RedisSerializer<String> keySerializer = RedisSerializer.string();
        RedisSerializer<WarpPoint> valueSerializer = new Jackson2JsonRedisSerializer<>(objectMapper, WarpPoint.class);
        RedisSerializationContext<String, WarpPoint> serializationContext = RedisSerializationContext.<String, WarpPoint>newSerializationContext()
            .key(keySerializer)
            .value(valueSerializer)
            .hashKey(keySerializer)
            .hashValue(valueSerializer)
            .build();
        return new ReactiveRedisTemplate<>(connectionFactory, serializationContext);
    }

}
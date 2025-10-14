package io.github.flux7k.teleportmanager.core.redis;

import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration;
import org.springframework.context.annotation.Configuration;

@ImportAutoConfiguration(exclude = RedisRepositoriesAutoConfiguration.class)
@Configuration
public class RedisConfiguration {}

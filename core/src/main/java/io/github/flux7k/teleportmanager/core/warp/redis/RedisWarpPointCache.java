package io.github.flux7k.teleportmanager.core.warp.redis;

import io.github.flux7k.teleportmanager.core.warp.WarpPoint;
import io.github.flux7k.teleportmanager.core.warp.WarpPointCache;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;

@Component
class RedisWarpPointCache implements WarpPointCache {
    private final ReactiveRedisTemplate<String, WarpPoint> redisTemplate;
    private final Duration cacheTtl;

    private static final String CACHE_PREFIX = "warp:";
    private static final String ALL_WARPS_KEY = "warp:all";

    public RedisWarpPointCache(ReactiveRedisTemplate<String, WarpPoint> redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.cacheTtl = Duration.ofMinutes(30);
    }

    @Override
    public Mono<WarpPoint> get(String name) {
        return redisTemplate.opsForValue().get(cacheKey(name));
    }

    @Override
    public Mono<Void> put(String name, WarpPoint warpPoint) {
        return redisTemplate.opsForValue()
            .set(cacheKey(name), warpPoint, cacheTtl)
            .then();
    }

    @Override
    public Mono<Void> evict(String name) {
        return redisTemplate.delete(cacheKey(name)).then();
    }

    @Override
    public Mono<Void> evictAll() {
        return redisTemplate.keys(CACHE_PREFIX + "*")
            .flatMap(redisTemplate::delete)
            .then();
    }

    @Override
    public Flux<WarpPoint> getAll() {
        return redisTemplate.hasKey(ALL_WARPS_KEY)
            .flatMapMany(exists -> {
                if (exists) {
                    return redisTemplate.opsForList().range(ALL_WARPS_KEY, 0, -1);
                }
                return Flux.empty();
            });
    }

    @Override
    public Mono<Void> putAll(List<WarpPoint> warpPoints) {
        if (warpPoints.isEmpty()) {
            return Mono.empty();
        }
        return redisTemplate.opsForList()
            .rightPushAll(ALL_WARPS_KEY, warpPoints)
            .then(redisTemplate.expire(ALL_WARPS_KEY, cacheTtl))
            .then();
    }

    private String cacheKey(String name) {
        return CACHE_PREFIX + name;
    }

}

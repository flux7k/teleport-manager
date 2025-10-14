package io.github.flux7k.teleportmanager.core.warp;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface WarpPointCache {

    Mono<WarpPoint> get(String name);

    Mono<Void> put(String name, WarpPoint warpPoint);

    Mono<Void> evict(String name);

    Mono<Void> evictAll();

    Flux<WarpPoint> getAll();

    Mono<Void> putAll(List<WarpPoint> warpPoints);

}
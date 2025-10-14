package io.github.flux7k.teleportmanager.core.warp;

import io.github.flux7k.teleportmanager.core.clustering.ServerLoc;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface WarpPointRepository {

    Mono<WarpPoint> save(String name, ServerLoc location);

    Mono<WarpPoint> findByName(String name);

    Flux<WarpPoint> findAll();

    Mono<Void> deleteByName(String name);

    Mono<Boolean> existsByName(String name);

    Mono<WarpPoint> updateLocation(String name, ServerLoc location);

    Mono<WarpPoint> updateName(String oldName, String newName);

}
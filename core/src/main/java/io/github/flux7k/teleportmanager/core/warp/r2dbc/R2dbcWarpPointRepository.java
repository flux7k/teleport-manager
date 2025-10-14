package io.github.flux7k.teleportmanager.core.warp.r2dbc;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

interface R2dbcWarpPointRepository extends ReactiveCrudRepository<R2dbcWarpPointEntity, Integer> {

    Mono<R2dbcWarpPointEntity> findByName(String name);

    Mono<Boolean> existsByName(String name);

    Mono<Void> deleteByName(String name);

}
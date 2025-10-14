package io.github.flux7k.teleportmanager.core.warp;

import io.github.flux7k.teleportmanager.core.clustering.ServerLoc;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface WarpPointService {

    Mono<WarpPoint> createWarpPoint(String name, ServerLoc location);

    Mono<WarpPoint> getWarpPointByName(String name);

    Flux<WarpPoint> getAllWarpPoints();

    Mono<Void> deleteWarpPoint(String name);

    Mono<WarpPoint> updateWarpPointLocation(String name, ServerLoc newLocation);

    Mono<WarpPoint> updateWarpPointName(String oldName, String newName);

    Mono<Boolean> existsWarpPoint(String name);

}
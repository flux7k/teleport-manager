package io.github.flux7k.teleportmanager.plugin.warp;

import io.github.flux7k.teleportmanager.plugin.clustering.ClusteredServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class CrossServerWarpMessageConsumer {
    private static final Logger logger = LoggerFactory.getLogger(CrossServerWarpMessageConsumer.class);
    private final CrossServerWarpManager crossServerWarpManager;
    private final ClusteredServer clusteredServer;

    public CrossServerWarpMessageConsumer(CrossServerWarpManager crossServerWarpManager, ClusteredServer clusteredServer) {
        this.crossServerWarpManager = crossServerWarpManager;
        this.clusteredServer = clusteredServer;
    }

    public Mono<Void> receive(CrossServerWarpMessage message) {
        CrossServerWarp warp = new CrossServerWarp(message.playerId(), message.warpPoint(), message.timestamp());
        if (!clusteredServer.ifLocationLocal(message.warpPoint().location())) {
            return Mono.empty();
        }
        logger.debug("Adding pending warp for player {}: {}", message.playerId(), warp.warpPoint().name());
        crossServerWarpManager.addPendingWarp(message.playerId(), warp);
        return Mono.empty();
    }

}
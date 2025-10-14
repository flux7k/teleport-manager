package io.github.flux7k.teleportmanager.plugin.warp;

import io.github.flux7k.teleportmanager.core.clustering.ServerLoc;
import io.github.flux7k.teleportmanager.core.warp.WarpPoint;
import io.github.flux7k.teleportmanager.core.warp.WarpPointService;
import io.github.flux7k.teleportmanager.plugin.clustering.ClusteredServer;
import io.github.flux7k.teleportmanager.plugin.teleport.TeleportService;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class WarpUseCase {

    private final WarpPointService warpPointService;
    private final TeleportService teleportService;
    private final CrossServerWarpMessageProducer crossServerWarpMessageProducer;
    private final ClusteredServer clusteredServer;

    private static final Logger logger = LoggerFactory.getLogger(WarpUseCase.class);

    public WarpUseCase(WarpPointService warpPointService,
                       TeleportService teleportService,
                       CrossServerWarpMessageProducer crossServerWarpMessageProducer,
                       ClusteredServer clusteredServer) {
        this.warpPointService = warpPointService;
        this.teleportService = teleportService;
        this.crossServerWarpMessageProducer = crossServerWarpMessageProducer;
        this.clusteredServer = clusteredServer;
    }

    @Transactional
    public Mono<WarpPoint> createWarpPoint(Location location, String name) {
        ServerLoc serverLoc = clusteredServer.toNodeLocation(location);
        return warpPointService.createWarpPoint(name, serverLoc);
    }

    @Transactional(readOnly = true)
    public Flux<WarpPoint> getAllWarpPoints() {
        return warpPointService.getAllWarpPoints();
    }

    @Transactional
    public Mono<Void> deleteWarpPointByName(String warpPointName) {
        return warpPointService.deleteWarpPoint(warpPointName);
    }

    @Transactional(readOnly = true)
    public Mono<Void> warpByWarpPointName(Player player, String warpPointName) {
        return warpPointService.getWarpPointByName(warpPointName)
            .flatMap(warpPoint -> {
                ServerLoc location = warpPoint.location();

                if (clusteredServer.isLocationLocal(location)) {
                    logger.debug("Local warp: player={}, warpPointName={}", player.getName(), warpPointName);
                    return teleportService.teleport(player, location);
                }

                logger.debug("Cross server warp: player={}, warpPointName={}, targetServer={}", player.getName(), warpPointName, location.nodeName());

                return executeCrossServerWarp(player, warpPoint);
            })
            .then();
    }

    private Mono<Void> executeCrossServerWarp(Player player, WarpPoint warpPoint) {
        long timestamp = System.currentTimeMillis();
        ServerLoc location = warpPoint.location();

        return crossServerWarpMessageProducer
            .send(new CrossServerWarpMessage(player.getUniqueId(), warpPoint, timestamp))
            .then(clusteredServer.transfer(player, location.nodeName()));
    }
}
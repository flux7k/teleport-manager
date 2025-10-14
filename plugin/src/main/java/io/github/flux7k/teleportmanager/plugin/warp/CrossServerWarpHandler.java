package io.github.flux7k.teleportmanager.plugin.warp;

import io.github.flux7k.teleportmanager.plugin.teleport.TeleportService;
import org.bukkit.entity.Player;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class CrossServerWarpHandler {
    private static final Logger logger = LoggerFactory.getLogger(CrossServerWarpHandler.class);

    private final CrossServerWarpManager crossServerWarpManager;
    private final TeleportService teleportService;

    public CrossServerWarpHandler(CrossServerWarpManager crossServerWarpManager, TeleportService teleportService) {
        this.crossServerWarpManager = crossServerWarpManager;
        this.teleportService = teleportService;
    }

    public Mono<Void> handleFromDestinationServer(Player player) {
        CrossServerWarp crossServerWarp = crossServerWarpManager.getPendingWarp(player.getUniqueId());
        if (crossServerWarp == null) {
            logger.debug("No pending cross server warp for player: {}", player.getUniqueId());
            return Mono.empty();
        }

        return teleportService.teleport(player, crossServerWarp.warpPoint().location()).then();
    }

}
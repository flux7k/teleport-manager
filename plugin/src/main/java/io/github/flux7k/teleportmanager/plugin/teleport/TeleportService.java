package io.github.flux7k.teleportmanager.plugin.teleport;

import io.github.flux7k.teleportmanager.core.clustering.ServerLoc;
import org.bukkit.entity.Player;
import reactor.core.publisher.Mono;

public interface TeleportService {

    Mono<Boolean> teleport(Player player, ServerLoc location);

}
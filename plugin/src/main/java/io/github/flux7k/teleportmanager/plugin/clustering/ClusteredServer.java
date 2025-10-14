package io.github.flux7k.teleportmanager.plugin.clustering;

import io.github.flux7k.teleportmanager.core.clustering.ServerLoc;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import reactor.core.publisher.Mono;

public interface ClusteredServer {

    Mono<Void> transfer(Player player, String targetServer);

    ServerLoc toNodeLocation(Location location);

    Boolean ifLocationLocal(ServerLoc serverLoc);

}
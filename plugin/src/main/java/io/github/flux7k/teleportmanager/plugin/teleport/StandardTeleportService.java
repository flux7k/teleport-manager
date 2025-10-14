package io.github.flux7k.teleportmanager.plugin.teleport;

import io.github.flux7k.teleportmanager.core.clustering.ServerLoc;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
class StandardTeleportService implements TeleportService {

    @Override
    public Mono<Boolean> teleport(Player player, ServerLoc location) {
        World world = Bukkit.getWorld(location.world());
        if (world == null) {
            return Mono.error(new IllegalStateException("World not found: " + location.world()));
        }
        Location unwrapped = new Location(world, location.x(), location.y(), location.z(), location.yaw(), location.pitch());
        return Mono.fromFuture(player.teleportAsync(unwrapped));
    }

}
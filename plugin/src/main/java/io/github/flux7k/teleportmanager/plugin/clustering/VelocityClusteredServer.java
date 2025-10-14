package io.github.flux7k.teleportmanager.plugin.clustering;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import io.github.flux7k.teleportmanager.core.clustering.ServerLoc;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.DiscardedPayload;
import net.minecraft.resources.ResourceLocation;
import org.bukkit.Location;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
class VelocityClusteredServer implements ClusteredServer {
    private static final Logger logger = LoggerFactory.getLogger(VelocityClusteredServer.class);
    private final ClusteringConfiguration clusteringConfiguration;

    public VelocityClusteredServer(ClusteringConfiguration clusteringConfiguration) {
        this.clusteringConfiguration = clusteringConfiguration;
    }

    @Override
    public Mono<Void> transfer(Player player, String targetServer) {
        ByteArrayDataOutput dataOutput = ByteStreams.newDataOutput();
        dataOutput.writeUTF("Connect");
        dataOutput.writeUTF(targetServer);
        ResourceLocation resourceLocation = ResourceLocation.parse("bungeecord:main");
        ClientboundCustomPayloadPacket packet = new ClientboundCustomPayloadPacket(new DiscardedPayload(resourceLocation, dataOutput.toByteArray()));
        CraftPlayer craftPlayer = (CraftPlayer) player;

        return Mono.create(sink -> craftPlayer.getHandle().connection.send(packet, (channelFuture) -> {
            if (channelFuture.isSuccess()) {
                logger.debug("Successfully sent transfer packet: player={}, targetServer={}",
                    player.getName(), targetServer);
                sink.success();
            } else {
                logger.error("Failed to send transfer packet: player={}, targetServer={}",
                    player.getName(), targetServer, channelFuture.cause());
                sink.error(new IllegalStateException("Failed to transfer player: " + player.getName(), channelFuture.cause()));
            }
        }));
    }

    @Override
    public ServerLoc toNodeLocation(Location location) {
        return new ServerLoc(
            clusteringConfiguration.getName(),
            location.getWorld().getName(),
            location.getX(),
            location.getY(),
            location.getZ(),
            location.getYaw(),
            location.getPitch()
        );
    }

    @Override
    public Boolean ifLocationLocal(ServerLoc serverLoc) {
        logger.debug("Checking if location is local: serverLoc={}, localNode={}", serverLoc.nodeName(), clusteringConfiguration.getName());
        return serverLoc.nodeName().equals(clusteringConfiguration.getName());
    }
}

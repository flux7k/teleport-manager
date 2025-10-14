package io.github.flux7k.teleportmanager.plugin.warp.protocol;

import io.github.flux7k.teleportmanager.plugin.warp.CrossServerWarpHandler;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import net.minecraft.network.protocol.game.ClientboundServerDataPacket;
import net.minecraft.network.protocol.login.ServerboundHelloPacket;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
class PlayerServerDataPacketHandler extends ChannelDuplexHandler {
    private static final Logger logger = LoggerFactory.getLogger(PlayerServerDataPacketHandler.class);

    private final CrossServerWarpHandler crossServerWarpHandler;

    @Nullable
    private UUID playerId;

    public PlayerServerDataPacketHandler(CrossServerWarpHandler crossServerWarpHandler) {
        this.crossServerWarpHandler = crossServerWarpHandler;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof ServerboundHelloPacket packet) {
            this.playerId = packet.profileId();
        }
        super.channelRead(ctx, msg);
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if (playerId == null) {
            super.write(ctx, msg, promise);
            return;
        }
        if (msg instanceof ClientboundServerDataPacket) {
            Player player = Bukkit.getPlayer(playerId);
            if (player == null) {
                promise.setFailure(new IllegalStateException("Player not found: " + playerId));
                logger.error("Player not found: {}", playerId);
                return;
            }
            crossServerWarpHandler.handleFromDestinationServer(player)
                .doFinally(s -> {
                    try {
                        super.write(ctx, msg, promise);
                    } catch (Exception exception) {
                        throw new RuntimeException(exception);
                    }
                })
                .onErrorResume(error -> {
                    logger.error("Error handling cross server warp for player: {}", playerId, error);
                    promise.setFailure(error);
                    return Mono.empty();
                })
                .subscribe();
        } else {
            super.write(ctx, msg, promise);
        }

    }

}
package io.github.flux7k.teleportmanager.plugin.warp.protocol;

import io.github.flux7k.teleportmanager.plugin.warp.CrossServerWarpHandler;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.papermc.paper.network.ChannelInitializeListener;
import io.papermc.paper.network.ChannelInitializeListenerHolder;
import jakarta.annotation.PostConstruct;
import net.minecraft.network.HandlerNames;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.springframework.stereotype.Component;

@Component
class PlayerServerDataPacketHandlerRegistrar implements ChannelInitializeListener {

    private final CrossServerWarpHandler crossServerWarpHandler;

    PlayerServerDataPacketHandlerRegistrar(CrossServerWarpHandler crossServerWarpHandler) {
        this.crossServerWarpHandler = crossServerWarpHandler;
    }

    @PostConstruct
    void register() {
        Plugin plugin = JavaPlugin.getProvidingPlugin(this.getClass());
        NamespacedKey namespaceKey = new NamespacedKey(plugin, "player_server_data_packet_handler_initializer");
        ChannelInitializeListenerHolder.addListener(namespaceKey.key(), this);
    }

    @Override
    public void afterInitChannel(@NonNull Channel channel) {
        ChannelHandler channelHandler = channel.pipeline().get(HandlerNames.PACKET_HANDLER);
        if (channelHandler != null) {
            channel.pipeline().addBefore(HandlerNames.PACKET_HANDLER,
                PlayerServerDataPacketHandler.class.getSimpleName(),
                new PlayerServerDataPacketHandler(crossServerWarpHandler)
            );
        }
    }

}

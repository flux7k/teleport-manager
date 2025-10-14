package io.github.flux7k.teleportmanager.plugin.warp.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.github.flux7k.teleportmanager.core.warp.errors.WarpException;
import io.github.flux7k.teleportmanager.plugin.warp.WarpUseCase;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.BlockPositionResolver;
import io.papermc.paper.math.BlockPosition;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import jakarta.annotation.PostConstruct;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

@org.springframework.stereotype.Component
public class WarpCommand {
    private static final Logger logger = LoggerFactory.getLogger(WarpCommand.class);

    private final WarpUseCase warpUseCase;
    private final WarpNameArgumentType warpNameArgumentType;

    public WarpCommand(WarpUseCase warpUseCase, WarpNameArgumentType warpNameArgumentType) {
        this.warpUseCase = warpUseCase;
        this.warpNameArgumentType = warpNameArgumentType;
    }

    @SuppressWarnings("UnstableApiUsage")
    @PostConstruct
    public void register() {
        Plugin plugin = JavaPlugin.getProvidingPlugin(this.getClass());
        plugin.getLifecycleManager().registerEventHandler(
            LifecycleEvents.COMMANDS,
            event -> event.registrar().register(createCommand())
        );
    }

    @SuppressWarnings("UnstableApiUsage")
    private LiteralCommandNode<CommandSourceStack> createCommand() {
        return Commands.literal("warp")
            .requires(source -> source.getSender() instanceof Player)
            .then(
                Commands.literal("create")
                    .then(Commands.argument("name", StringArgumentType.word())
                        .then(Commands.argument("position", ArgumentTypes.blockPosition())
                            .then(Commands.argument("world", ArgumentTypes.world())
                                .executes(context -> {
                                    CommandSender sender = context.getSource().getSender();
                                    String name = StringArgumentType.getString(context, "name");
                                    World world = context.getArgument("world", World.class);
                                    BlockPositionResolver blockPositionResolver = context.getArgument("position", BlockPositionResolver.class);
                                    BlockPosition blockPosition = blockPositionResolver.resolve(context.getSource());
                                    Location location = blockPosition.toLocation(world);
                                    createWarpPoint(sender, location, name);
                                    return Command.SINGLE_SUCCESS;
                                })
                            )
                        )
                        .executes(context -> {
                            CommandSender player = context.getSource().getSender();
                            String name = StringArgumentType.getString(context, "name");
                            Location location = ((Player) player).getLocation();
                            createWarpPoint(player, location, name);
                            return Command.SINGLE_SUCCESS;
                        })
                    )
            )
            .then(
                Commands.literal("list")
                    .executes(context -> {
                        CommandSender sender = context.getSource().getSender();
                        printWarpPointList(sender);
                        return Command.SINGLE_SUCCESS;
                    })
            )
            .then(
                Commands.literal("delete")
                    .then(Commands.argument("name", warpNameArgumentType)
                        .executes(context -> {
                            CommandSender sender = context.getSource().getSender();
                            String name = context.getArgument("name", String.class);
                            deleteWarpPoint(sender, name);
                            return Command.SINGLE_SUCCESS;
                        })
                    )
            )
            .then(
                Commands.literal("goto")
                    .then(Commands.argument("name", warpNameArgumentType)
                        .executes(context -> {
                            CommandSender sender = context.getSource().getSender();
                            String name = context.getArgument("name", String.class);
                            teleportToWarpPoint((Player) sender, name);
                            return Command.SINGLE_SUCCESS;
                        })
                    )
            )
            .build();
    }

    private void createWarpPoint(CommandSender sender, Location location, String name) {
        warpUseCase
            .createWarpPoint(location, name)
            .doOnSuccess(warpPoint -> {
                Component translatable = Component
                    .translatable("teleport_manager.warp_point_creation_succeeded")
                    .arguments(Component.text(warpPoint.name()))
                    .arguments(Component.text(warpPoint.id()));
                sender.sendMessage(translatable);
            })
            .doOnError(error -> {
                if (error instanceof WarpException) {
                    return;
                }
                logger.error("Unexpected error during warp creation", error);
            })
            .doOnError(error -> {
                Component translatable = Component
                    .translatable("teleport_manager.warp_point_creation_failed", "%s")
                    .arguments(Component.text(error.getMessage()));
                sender.sendMessage(translatable);
            })
            .onErrorResume(error -> Mono.empty())
            .subscribe();
    }

    private void printWarpPointList(CommandSender sender) {
        warpUseCase
            .getAllWarpPoints()
            .switchIfEmpty(Mono.defer(() -> {
                Component translatable = Component
                    .translatable("teleport_manager.no_warp_points_found");
                sender.sendMessage(translatable);
                return Mono.empty();
            }))
            .doOnNext(warpPoint -> {
                Component translatable = Component
                    .translatable("teleport_manager.warp_point_listing_element", "%s")
                    .arguments(Component.text(warpPoint.toString()));
                sender.sendMessage(translatable);
            })
            .doOnError(error -> {
                if (error instanceof WarpException) {
                    return;
                }
                logger.error("Unexpected error during printing warp point list", error);
            })
            .onErrorResume(error -> Mono.empty())
            .subscribe();
    }

    private void deleteWarpPoint(CommandSender sender, String name) {
        warpUseCase
            .deleteWarpPointByName(name)
            .doOnSuccess(v -> {
                Component translatable = Component
                    .translatable("teleport_manager.warp_point_deletion_succeeded")
                    .arguments(Component.text(name));
                sender.sendMessage(translatable);
            })
            .doOnError(error -> {
                if (error instanceof WarpException) {
                    return;
                }
                logger.error("Unexpected error during warp point deletion", error);
            })
            .doOnError(error -> {
                Component translatable = Component
                    .translatable("teleport_manager.warp_point_deletion_failed", "%s")
                    .arguments(Component.text(error.getMessage()));
                sender.sendMessage(translatable);
            })
            .onErrorResume(error -> Mono.empty())
            .subscribe();
    }

    private void teleportToWarpPoint(Player player, String name) {
        warpUseCase
            .warpByWarpPointName(player, name)
            .doOnSuccess(v -> {
                Component translatable = Component
                    .translatable("teleport_manager.warp_teleportation_succeeded")
                    .arguments(Component.text(name));
                player.sendMessage(translatable);
            })
            .doOnError(error -> {
                if (error instanceof WarpException) {
                    return;
                }
                logger.error("Unexpected error during warp teleportation", error);
            })
            .doOnError(error -> {
                Component translatable = Component
                    .translatable("teleport_manager.warp_teleportation_failed", "%s")
                    .arguments(Component.text(error.getMessage()));
                player.sendMessage(translatable);
            })
            .onErrorResume(error -> Mono.empty())
            .subscribe();
    }

}
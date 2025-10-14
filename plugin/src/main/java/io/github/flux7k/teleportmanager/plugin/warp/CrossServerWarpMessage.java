package io.github.flux7k.teleportmanager.plugin.warp;

import io.github.flux7k.teleportmanager.core.warp.WarpPoint;

import java.util.UUID;

public record CrossServerWarpMessage(
    UUID playerId,
    WarpPoint warpPoint,
    long timestamp
) {}
package io.github.flux7k.teleportmanager.plugin.warp;

import io.github.flux7k.teleportmanager.core.warp.WarpPoint;

import java.util.UUID;

public record CrossServerWarp(
    UUID playerId,
    WarpPoint warpPoint,
    long timestamp
) {

    public boolean isExpired(long expiryDuration) {
        return System.currentTimeMillis() - timestamp > expiryDuration;
    }

}
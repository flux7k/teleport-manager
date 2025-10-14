package io.github.flux7k.teleportmanager.core.warp;

import io.github.flux7k.teleportmanager.core.clustering.ServerLoc;

public record WarpPoint(
    Integer id,
    String name,
    ServerLoc location
) {}
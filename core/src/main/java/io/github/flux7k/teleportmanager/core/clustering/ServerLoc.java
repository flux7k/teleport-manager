package io.github.flux7k.teleportmanager.core.clustering;

public record ServerLoc(
    String nodeName,
    String world,
    double x,
    double y,
    double z,
    float yaw,
    float pitch
) {}
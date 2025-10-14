package io.github.flux7k.teleportmanager.core.warp.errors;

public class WarpPointNotFoundException extends WarpException {
    public WarpPointNotFoundException(String name) {
        super("Warp not found: " + name);
    }
}

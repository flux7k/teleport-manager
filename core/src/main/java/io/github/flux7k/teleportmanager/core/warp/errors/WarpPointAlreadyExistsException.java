package io.github.flux7k.teleportmanager.core.warp.errors;

public class WarpPointAlreadyExistsException extends WarpException {
    public WarpPointAlreadyExistsException(String name) {
        super("Warp name already exists: " + name);
    }
}
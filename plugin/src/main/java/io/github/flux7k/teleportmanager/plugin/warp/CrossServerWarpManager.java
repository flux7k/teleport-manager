package io.github.flux7k.teleportmanager.plugin.warp;

import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class CrossServerWarpManager {

    private static final Logger logger = LoggerFactory.getLogger(CrossServerWarpManager.class);
    private static final long EXPIRY_DURATION_MS = 5000;
    private static final long EXPIRY_CLEANUP_INTERVAL_MS = 60000;

    private final Map<UUID, CrossServerWarp> pendingWarps = new ConcurrentHashMap<>();

    public void addPendingWarp(UUID playerId, CrossServerWarp crossServerWarp) {
        pendingWarps.put(playerId, crossServerWarp);
    }

    public @Nullable CrossServerWarp getPendingWarp(UUID playerId) {
        CrossServerWarp crossServerWarp = pendingWarps.get(playerId);
        if (crossServerWarp != null && crossServerWarp.isExpired(EXPIRY_DURATION_MS)) {
            logger.warn("Pending warp for player {} has expired: {}", playerId, crossServerWarp);
            pendingWarps.remove(playerId);
            return null;
        }
        return crossServerWarp;
    }

    @Scheduled(fixedDelay = EXPIRY_CLEANUP_INTERVAL_MS)
    public void cleanupExpired() {
        int beforeCleanupSize = pendingWarps.size();
        pendingWarps.entrySet().removeIf(entry -> entry.getValue().isExpired(EXPIRY_DURATION_MS));
        int afterCleanupSize = pendingWarps.size();
        if (beforeCleanupSize != afterCleanupSize) {
            logger.debug("Cleaned up {} expired pending warps", beforeCleanupSize - afterCleanupSize);
        }
    }

}
package io.github.flux7k.teleportmanager.core.warp;

import io.github.flux7k.teleportmanager.core.clustering.ServerLoc;
import io.github.flux7k.teleportmanager.core.warp.errors.WarpPointAlreadyExistsException;
import io.github.flux7k.teleportmanager.core.warp.errors.WarpPointNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class WarpPointServiceImpl implements WarpPointService {

    private static final Logger logger = LoggerFactory.getLogger(WarpPointServiceImpl.class);

    private final WarpPointRepository repository;
    private final WarpPointCache cache;

    public WarpPointServiceImpl(WarpPointRepository repository, WarpPointCache cache) {
        this.repository = repository;
        this.cache = cache;
    }

    @Override
    public Mono<WarpPoint> createWarpPoint(String name, ServerLoc location) {
        logger.debug("Creating warp point: name={}, location={}", name, location);
        return repository.existsByName(name)
            .flatMap(exists -> {
                if (exists) {
                    logger.warn("Warp point creation failed - already exists: name={}", name);
                    return Mono.error(new WarpPointAlreadyExistsException(name));
                }
                return repository.save(name, location);
            })
            .flatMap(warpPoint ->
                cache.put(name, warpPoint)
                    .then(cache.evictAll())
                    .thenReturn(warpPoint)
            )
            .doOnSuccess(warpPoint -> logger.info("Warp point created successfully: name={}, id={}", name, warpPoint.id()));
    }

    @Override
    public Mono<WarpPoint> getWarpPointByName(String name) {
        logger.debug("Getting warp point: name={}", name);
        return cache.get(name)
            .doOnNext(warpPoint -> logger.debug("Warp point found in cache: name={}", name))
            .switchIfEmpty(
                Mono.defer(() -> {
                    logger.debug("Warp point not in cache, fetching from repository: name={}", name);
                    return repository.findByName(name)
                        .switchIfEmpty(Mono.defer(() -> {
                            logger.warn("Warp point not found: name={}", name);
                            return Mono.error(new WarpPointNotFoundException(name));
                        }))
                        .flatMap(warpPoint ->
                            cache.put(name, warpPoint)
                                .doOnSuccess(v -> logger.debug("Warp point cached: name={}", name))
                                .thenReturn(warpPoint)
                        );
                })
            );
    }

    @Override
    public Flux<WarpPoint> getAllWarpPoints() {
        logger.debug("Getting all warp points");
        return cache.getAll()
            .switchIfEmpty(
                Flux.defer(() -> {
                    logger.debug("Cache miss, fetching all warp points from the repository");
                    return repository.findAll()
                        .collectList()
                        .flatMapMany(list -> {
                            logger.debug("Found {} warp points in repository", list.size());
                            return cache.putAll(list)
                                .doOnSuccess(v -> logger.debug("All warp points pushed to the cache."))
                                .thenMany(Flux.fromIterable(list));
                        });
                })
            );
    }

    @Override
    public Mono<Void> deleteWarpPoint(String name) {
        logger.debug("Deleting warp point: name={}", name);
        return repository.existsByName(name)
            .flatMap(exists -> {
                if (!exists) {
                    return Mono.error(new WarpPointNotFoundException(name));
                }
                return repository.deleteByName(name);
            })
            .then(cache.evict(name))
            .then(cache.evictAll())
            .doOnSuccess(v -> logger.info("Warp point deleted successfully: name={}", name));
    }

    @Override
    public Mono<WarpPoint> updateWarpPointLocation(String name, ServerLoc newLocation) {
        logger.debug("Updating warp point location: name={}, newLocation={}", name, newLocation);
        return repository.updateLocation(name, newLocation)
            .switchIfEmpty(Mono.defer(() -> {
                logger.warn("Warp point location update failed - not found: name={}", name);
                return Mono.error(new WarpPointNotFoundException(name));
            }))
            .flatMap(warpPoint ->
                cache.put(name, warpPoint)
                    .then(cache.evictAll())
                    .thenReturn(warpPoint)
            )
            .doOnSuccess(warpPoint -> logger.info("Warp point location updated successfully: name={}, id={}", name, warpPoint.id()));
    }

    @Override
    public Mono<WarpPoint> updateWarpPointName(String oldName, String newName) {
        logger.debug("Updating warp point name: oldName={}, newName={}", oldName, newName);
        return repository.existsByName(newName)
            .flatMap(exists -> {
                if (exists) {
                    logger.warn("Warp point name update failed - new name already exists: newName={}", newName);
                    return Mono.error(new WarpPointAlreadyExistsException(newName));
                }
                return repository.updateName(oldName, newName)
                    .switchIfEmpty(Mono.defer(() -> {
                        logger.warn("Warp point name update failed - not found: oldName={}", oldName);
                        return Mono.error(new WarpPointNotFoundException(oldName));
                    }));
            })
            .flatMap(warpPoint ->
                cache.evict(oldName)
                    .then(cache.put(newName, warpPoint))
                    .then(cache.evictAll())
                    .thenReturn(warpPoint)
            )
            .doOnSuccess(warpPoint -> logger.info("Warp point name updated successfully: oldName={}, newName={}, id={}", oldName, newName, warpPoint.id()));
    }

    @Override
    public Mono<Boolean> existsWarpPoint(String name) {
        logger.debug("Checking if warp point exists: name={}", name);
        return cache.get(name)
            .hasElement()
            .flatMap(exists -> {
                if (exists) {
                    logger.debug("Warp point exists in cache: name={}", name);
                    return Mono.just(true);
                }
                logger.debug("Warp point not in cache, checking repository: name={}", name);
                return repository.existsByName(name);
            })
            .doOnSuccess(exists -> logger.debug("Warp point exists check result: name={}, exists={}", name, exists));
    }
}
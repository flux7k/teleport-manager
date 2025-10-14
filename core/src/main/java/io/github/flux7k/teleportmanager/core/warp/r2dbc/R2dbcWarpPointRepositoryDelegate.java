package io.github.flux7k.teleportmanager.core.warp.r2dbc;

import io.github.flux7k.teleportmanager.core.clustering.ServerLoc;
import io.github.flux7k.teleportmanager.core.warp.WarpPoint;
import io.github.flux7k.teleportmanager.core.warp.WarpPointRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
class R2dbcWarpPointRepositoryDelegate implements WarpPointRepository {

    private final R2dbcWarpPointRepository r2dbcRepository;

    public R2dbcWarpPointRepositoryDelegate(R2dbcWarpPointRepository r2dbcRepository) {
        this.r2dbcRepository = r2dbcRepository;
    }

    @Override
    public Mono<WarpPoint> save(String name, ServerLoc location) {
        return r2dbcRepository.save(R2dbcWarpPointEntity.wrapNew(name, location))
            .map(R2dbcWarpPointEntity::unwrap);
    }

    @Override
    public Mono<WarpPoint> findByName(String name) {
        return r2dbcRepository.findByName(name)
            .map(R2dbcWarpPointEntity::unwrap);
    }

    @Override
    public Flux<WarpPoint> findAll() {
        return r2dbcRepository.findAll()
            .map(R2dbcWarpPointEntity::unwrap);
    }

    @Override
    public Mono<Void> deleteByName(String name) {
        return r2dbcRepository.deleteByName(name);
    }

    @Override
    public Mono<Boolean> existsByName(String name) {
        return r2dbcRepository.existsByName(name);
    }

    @Override
    public Mono<WarpPoint> updateLocation(String name, ServerLoc location) {
        return r2dbcRepository.findByName(name)
            .flatMap(entity -> {
                R2dbcWarpPointEntity updated = new R2dbcWarpPointEntity(
                    entity.id(),
                    entity.name(),
                    location.nodeName(),
                    location.world(),
                    location.x(),
                    location.y(),
                    location.z(),
                    location.yaw(),
                    location.pitch()
                );
                return r2dbcRepository.save(updated);
            })
            .map(R2dbcWarpPointEntity::unwrap);
    }

    @Override
    public Mono<WarpPoint> updateName(String oldName, String newName) {
        return r2dbcRepository.findByName(oldName)
            .flatMap(entity -> {
                R2dbcWarpPointEntity updated = new R2dbcWarpPointEntity(
                    entity.id(),
                    newName,
                    entity.server(),
                    entity.world(),
                    entity.x(),
                    entity.y(),
                    entity.z(),
                    entity.yaw(),
                    entity.pitch()
                );
                return r2dbcRepository.save(updated);
            })
            .map(R2dbcWarpPointEntity::unwrap);
    }

}

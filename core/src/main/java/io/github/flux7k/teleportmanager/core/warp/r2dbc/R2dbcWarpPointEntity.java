package io.github.flux7k.teleportmanager.core.warp.r2dbc;

import io.github.flux7k.teleportmanager.core.clustering.ServerLoc;
import io.github.flux7k.teleportmanager.core.warp.WarpPoint;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("warp_point")
record R2dbcWarpPointEntity(
    @Id
    Integer id,
    String name,
    String server,
    String world,
    double x,
    double y,
    double z,
    float yaw,
    float pitch
) {
    public WarpPoint unwrap() {
        return new WarpPoint(id(), name(), new ServerLoc(server(), world(), x(), y(), z(), yaw(), pitch()));
    }

    static R2dbcWarpPointEntity wrapNew(String name, ServerLoc serverLoc) {
        return new R2dbcWarpPointEntity(
            null,
            name,
            serverLoc.nodeName(),
            serverLoc.world(),
            serverLoc.x(),
            serverLoc.y(),
            serverLoc.z(),
            serverLoc.yaw(),
            serverLoc.pitch()
        );
    }

}
package io.github.flux7k.teleportmanager.plugin.warp;

import reactor.core.publisher.Mono;

public interface CrossServerWarpMessageProducer {

    Mono<Void> send(CrossServerWarpMessage message);

}
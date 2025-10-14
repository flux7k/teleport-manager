package io.github.flux7k.teleportmanager.plugin.warp.redis;

import io.github.flux7k.teleportmanager.plugin.warp.CrossServerWarpMessage;
import io.github.flux7k.teleportmanager.plugin.warp.CrossServerWarpMessageConsumer;
import io.github.flux7k.teleportmanager.plugin.warp.CrossServerWarpMessageProducer;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.ReactiveSubscription;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Component;
import reactor.core.Disposable;
import reactor.core.publisher.Mono;

@Component
public class RedisCrossServerWarpMessageGateway implements CrossServerWarpMessageProducer {
    private static final Logger logger = LoggerFactory.getLogger(RedisCrossServerWarpMessageGateway.class);
    private static final String TOPIC = "teleport-manager:cross-server-warp";

    private final ReactiveRedisTemplate<String, CrossServerWarpMessage> redisTemplate;
    private final CrossServerWarpMessageConsumer crossServerWarpMessageConsumer;

    private Disposable subscription;

    public RedisCrossServerWarpMessageGateway(ReactiveRedisTemplate<String, CrossServerWarpMessage> redisTemplate,
                                              CrossServerWarpMessageConsumer crossServerWarpMessageConsumer) {
        this.redisTemplate = redisTemplate;
        this.crossServerWarpMessageConsumer = crossServerWarpMessageConsumer;
    }

    @Override
    public Mono<Void> send(CrossServerWarpMessage message) {
        logger.debug("Sending cross server warp message: playerId={}, warpPoint={}", message.playerId(), message.warpPoint().name());
        return redisTemplate.convertAndSend(TOPIC, message).then();
    }

    @PostConstruct
    public void subscribe() {
        this.subscription = redisTemplate.listenToChannel(TOPIC)
            .map(ReactiveSubscription.Message::getMessage)
            .doOnNext(msg -> logger.debug("Received cross server warp broadcast: {}", msg))
            .flatMap(crossServerWarpMessageConsumer::receive)
            .subscribe();
    }

    @PreDestroy
    public void unsubscribe() {
        if (subscription != null && !subscription.isDisposed()) {
            subscription.dispose();
        }
    }

}
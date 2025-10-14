package io.github.flux7k.teleportmanager.plugin.warp.command;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.github.flux7k.teleportmanager.core.warp.WarpPoint;
import io.github.flux7k.teleportmanager.core.warp.WarpPointService;
import io.papermc.paper.command.brigadier.argument.CustomArgumentType;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Sinks;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@SuppressWarnings("UnstableApiUsage")
@Component
public class WarpNameArgumentType implements CustomArgumentType<String, String> {

    private static final Logger logger = LoggerFactory.getLogger(WarpNameArgumentType.class);
    private static final Duration CACHE_DURATION = Duration.ofMillis(1000);

    private final WarpPointService warpPointService;

    private volatile Instant lastFetch = Instant.MIN;
    private volatile boolean isFetching = false;

    private final Sinks.Many<List<String>> debouncer = Sinks.many().replay().limit(1);

    public WarpNameArgumentType(WarpPointService warpPointService) {
        this.warpPointService = warpPointService;
    }

    @Override
    public @NotNull String parse(StringReader reader) throws CommandSyntaxException {
        return reader.readString();
    }

    @Override
    public <S> @NotNull String parse(StringReader reader, @NotNull S source) {
        return reader.readUnquotedString();
    }

    @Override
    public @NotNull ArgumentType<String> getNativeType() {
        return StringArgumentType.word();
    }

    @Override
    public <S> @NotNull CompletableFuture<Suggestions> listSuggestions(@NotNull CommandContext<S> context, @NotNull SuggestionsBuilder builder) {
        CompletableFuture<Suggestions> future = new CompletableFuture<>();
        Duration timeSinceLastFetch = Duration.between(lastFetch, Instant.now());
        if (timeSinceLastFetch.compareTo(CACHE_DURATION) > 0 && !isFetching) {
            fetchWarpNames();
        }

        debouncer.asFlux()
            .next()
            .timeout(Duration.ofMillis(500))
            .defaultIfEmpty(List.of())
            .subscribe(
                names -> {
                    String input = builder.getRemaining().toLowerCase();
                    names.stream()
                        .filter(name -> name.toLowerCase().startsWith(input))
                        .limit(10)
                        .forEach(builder::suggest);
                    future.complete(builder.build());
                },
                error -> {
                    logger.debug("Error getting warp suggestions from cache", error);
                    future.complete(builder.build());
                }
            );

        return future;
    }

    private void fetchWarpNames() {
        if (isFetching) {
            return;
        }

        isFetching = true;
        logger.debug("Fetching warp names");

        warpPointService.getAllWarpPoints()
            .map(WarpPoint::name)
            .collectList()
            .timeout(Duration.ofMillis(500))
            .subscribe(
                names -> {
                    lastFetch = Instant.now();
                    isFetching = false;
                    debouncer.tryEmitNext(names);
                    logger.debug("Cached {} warp names", names.size());
                },
                error -> {
                    isFetching = false;
                    logger.error("Failed to fetch warp names", error);
                    debouncer.tryEmitNext(List.of());
                }
            );
    }

}
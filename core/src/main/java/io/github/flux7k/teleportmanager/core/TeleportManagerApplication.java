package io.github.flux7k.teleportmanager.core;

import io.r2dbc.spi.ConnectionFactory;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.Bean;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.r2dbc.core.binding.BindMarkersFactory;

@ConfigurationPropertiesScan(basePackages = "io.github.flux7k.teleportmanager")
@SpringBootApplication(scanBasePackages = "io.github.flux7k.teleportmanager")
public class TeleportManagerApplication {

    @Bean
    public DatabaseClient databaseClient(ConnectionFactory connectionFactory) {
        return DatabaseClient.builder()
            .connectionFactory(connectionFactory)
            .bindMarkers(BindMarkersFactory.anonymous("?"))
            .build();
    }

}
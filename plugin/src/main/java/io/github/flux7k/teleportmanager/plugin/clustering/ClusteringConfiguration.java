package io.github.flux7k.teleportmanager.plugin.clustering;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "teleport-manager.clustering")
class ClusteringConfiguration {

    private final static Logger logger = LoggerFactory.getLogger(ClusteringConfiguration.class);

    private final String nodeName;

    public ClusteringConfiguration(String nodeName) {
        this.nodeName = nodeName;
    }

    @PostConstruct
    public void whoAmI() {
        logger.debug("I am {}", nodeName);
    }

    public String getName() {
        return nodeName;
    }

}
package io.github.flux7k.teleportmanager.plugin;

import io.github.flux7k.teleportmanager.core.TeleportManagerApplication;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.springframework.boot.Banner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.env.PropertySourceLoader;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.util.List;

public class TeleportManagerPlugin extends JavaPlugin implements ApplicationContextInitializer<GenericApplicationContext> {

    private ConfigurableApplicationContext application;

    @Override
    public void onEnable() {
        Thread.currentThread().setContextClassLoader(getClassLoader());
        this.application = new SpringApplicationBuilder(TeleportManagerApplication.class)
            .web(WebApplicationType.NONE)
            .initializers(this)
            .resourceLoader(new DefaultResourceLoader(getClassLoader()))
            .bannerMode(Banner.Mode.OFF)
            .registerShutdownHook(false)
            .run();
    }

    public void onDisable() {
        if (application != null) {
            application.close();
        }
    }

    @Override
    public void initialize(@NotNull GenericApplicationContext applicationContext) {
        PropertySourceLoader propertySourceLoader = new YamlPropertySourceLoader();
        try {
            Resource applicationResource = applicationContext.getResource("classpath:application.yml");
            List<PropertySource<?>> sources = propertySourceLoader.load(getName(), applicationResource);
            sources.forEach(applicationContext.getEnvironment().getPropertySources()::addFirst);
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

}
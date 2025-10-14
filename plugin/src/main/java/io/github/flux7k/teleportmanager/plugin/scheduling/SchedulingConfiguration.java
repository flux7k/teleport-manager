package io.github.flux7k.teleportmanager.plugin.scheduling;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

import java.util.concurrent.Executor;

@Configuration
public class SchedulingConfiguration implements SchedulingConfigurer {

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        Plugin plugin = JavaPlugin.getProvidingPlugin(this.getClass());
        Executor executor = command -> Bukkit.getAsyncScheduler().runNow(plugin, (task) -> command.run());

        taskRegistrar.setScheduler(executor);
    }

}
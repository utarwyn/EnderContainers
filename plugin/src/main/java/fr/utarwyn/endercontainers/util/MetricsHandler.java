package fr.utarwyn.endercontainers.util;

import fr.utarwyn.endercontainers.Managers;
import fr.utarwyn.endercontainers.dependency.DependenciesManager;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.DrilldownPie;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;

/**
 * Manages all metrics of the plugin.
 * Powered free of charge by bStats: https://bstats.org/
 *
 * @author Utarwyn <maximemalgorn@gmail.com>
 */
public class MetricsHandler {

    /*
     * The plugin's identifier on the "bStats" service
     * @see https://bstats.org/plugin/bukkit/EnderContainers
     */
    private static final int PLUGIN_ID = 1855;

    /**
     * Construct the metrics handler.
     *
     * @param plugin instance of the plugin
     */
    public MetricsHandler(JavaPlugin plugin) {
        Metrics metrics = new Metrics(plugin, PLUGIN_ID);

        // Add custom metrics
        metrics.addCustomChart(new DrilldownPie(
                "dependenciesUsage", this::getDependenciesUsage
        ));
    }

    /**
     * Retrieve the dependencies usage of the plugin.
     *
     * @return object needed by bStats to create the chart
     */
    private Map<String, Map<String, Integer>> getDependenciesUsage() {
        Map<String, Map<String, Integer>> map = new HashMap<>();

        Managers.get(DependenciesManager.class).getDependencies()
                .forEach(dependency -> {
                    Map<String, Integer> dependencyMap = new HashMap<>();
                    dependencyMap.put(dependency.getPlugin().getDescription().getVersion(), 1);
                    map.put(dependency.getPlugin().getName(), dependencyMap);
                });

        return map;
    }

}

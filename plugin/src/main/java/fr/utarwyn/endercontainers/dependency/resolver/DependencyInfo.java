package fr.utarwyn.endercontainers.dependency.resolver;

import fr.utarwyn.endercontainers.dependency.Dependency;
import org.bukkit.plugin.Plugin;

/**
 * Store information about a resolved working dependency.
 *
 * @author Utarwyn <maxime.malgorn@laposte.net>
 * @since 2.2.0
 */
public class DependencyInfo {

    /**
     * Resolved dependency instance
     */
    private Dependency dependency;

    /**
     * Plugin detected by the resolver used by the dependency instance
     */
    private Plugin plugin;

    /**
     * Version of the plugin detected
     */
    private String pluginVersion;

    public DependencyInfo(Dependency dependency, Plugin plugin, String pluginVersion) {
        this.dependency = dependency;
        this.plugin = plugin;
        this.pluginVersion = pluginVersion;
    }

    public Dependency getDependency() {
        return dependency;
    }

    public Plugin getPlugin() {
        return plugin;
    }

    public String getPluginVersion() {
        return pluginVersion;
    }

}

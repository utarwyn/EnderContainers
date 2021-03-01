package fr.utarwyn.endercontainers.dependency;

import org.bukkit.plugin.Plugin;

/**
 * Represents a dependency managed by the plugin.
 *
 * @author Utarwyn <maxime.malgorn@laposte.net>
 * @since 2.2.0
 */
public abstract class Dependency implements DependencyValidator {

    /**
     * Java plugin object of the dependency
     */
    protected final Plugin plugin;

    /**
     * Construct a dependency object.
     *
     * @param plugin plugin instance
     */
    protected Dependency(Plugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Retrieve the plugin instance of the dependency.
     *
     * @return Bukkit plugin object
     */
    public final Plugin getPlugin() {
        return this.plugin;
    }

    /**
     * Called when the dependency has been enabled.
     */
    public void onEnable() {
        // Not implemented
    }

    /**
     * Called when the dependency has been disabled.
     */
    public void onDisable() {
        // Not implemented
    }

}

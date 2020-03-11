package fr.utarwyn.endercontainers.api.dependency.dependency;

import org.bukkit.plugin.Plugin;

/**
 * Class which represents a plugin dependency
 *
 * @author Utarwyn
 * @since 2.0.0
 */
public abstract class Dependency implements DependencyListener {

    /**
     * The name of the plugin/dependency
     */
    private String name;

    /**
     * Plugin attached to the dependency object
     */
    private Plugin plugin;

    /**
     * Construct a new dependency.
     *
     * @param name   name of the dependency
     * @param plugin plugin instance
     */
    public Dependency(String name, Plugin plugin) {
        this.name = name;
        this.plugin = plugin;
    }

    /**
     * Called when the dependency have been enabled
     */
    public void onEnable() {
        // Not implemented
    }

    /**
     * Called when the dependency have been disabled
     */
    public void onDisable() {
        // Not implemented
    }

    /**
     * Returns the name of the dependency
     *
     * @return The name of the dependency
     */
    public String getName() {
        return this.name;
    }

    /**
     * Gets the version of the linked plugin.
     *
     * @return Version of the plugin used for the dependency!
     */
    public String getPluginVersion() {
        return this.plugin != null ? this.plugin.getDescription().getVersion() : null;
    }

    /**
     * Gets the plugin attached to the dependency if loaded and enabled
     *
     * @return the Bukkit plugin
     */
    protected Plugin getPlugin() {
        return this.plugin;
    }

}

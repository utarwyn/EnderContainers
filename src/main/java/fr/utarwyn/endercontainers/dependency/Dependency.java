package fr.utarwyn.endercontainers.dependency;

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
     * Called when the dependency have been enabled
     */
    public void onEnable() {

    }

    /**
     * Called when the dependency have been disabled
     */
    public void onDisable() {

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
     * Define the name of the plugin used by the dependency object
     *
     * @param name Name of the Bukkit plugin
     */
    void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the plugin attached to the dependency if loaded and enabled
     *
     * @return the Bukkit plugin
     */
    protected Plugin getPlugin() {
        return this.plugin;
    }

    /**
     * Attached a Plugin object to the dependency object
     *
     * @param plugin Bukkit plugin to attach
     */
    void setPlugin(Plugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Gets the version of the linked plugin.
     *
     * @return Version of the plugin used for the dependency!
     */
    protected String getPluginVersion() {
        return this.plugin != null ? this.plugin.getDescription().getVersion() : null;
    }

}

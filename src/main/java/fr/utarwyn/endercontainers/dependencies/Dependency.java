package fr.utarwyn.endercontainers.dependencies;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

/**
 * Class which represents a plugin dependency
 * @since 2.0.0
 * @author Utarwyn
 */
public abstract class Dependency implements DependencyListener {

	/**
	 * The name of the plugin/dependency
	 */
	private String name;

	/**
	 * Returns the name of the dependency
	 * @return The name of the dependency
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * Define the name of the plugin used by the dependency object
	 * @param name Name of the Bukkit plugin
	 */
	void setName(String name) {
		this.name = name;
	}

	/**
	 * Gets the version of the linked plugin.
	 * @return Version of the plugin used for the dependency!
	 */
	protected String getPluginVersion() {
		Plugin plugin = Bukkit.getPluginManager().getPlugin(this.name);
		return plugin != null ? plugin.getDescription().getVersion() : null;
	}

	/**
	 * Called when the dependency have been enabled
	 */
	public abstract void onEnable();

	/**
	 * Called when the dependency have been disabled
	 */
	public abstract void onDisable();

}

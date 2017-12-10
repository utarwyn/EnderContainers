package fr.utarwyn.endercontainers.dependencies;

import fr.utarwyn.endercontainers.EnderContainers;

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
	 * Construct a dependency object with a specified name of the plugin
	 * @param name Name of the plugin to work with
	 */
	Dependency(String name) {
		this.name = name;
	}

	/**
	 * Returns the name of the dependency
	 * @return The name of the dependency
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * Get the plugin more easily than using the static way.
	 * @return The EnderContainers main class
	 */
	protected EnderContainers getPlugin() {
		return EnderContainers.getInstance();
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

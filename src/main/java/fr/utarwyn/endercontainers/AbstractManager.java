package fr.utarwyn.endercontainers;

import org.bukkit.event.Listener;

import java.util.logging.Logger;

/**
 * Base class for creating a manager through the plugin.
 * It cannot be instanciated directly. Only Managers class can create an instance of it.
 * @since 2.0.0
 * @author Utarwyn
 */
public abstract class AbstractManager implements Listener {

	/**
	 * Stores the plugin main class
	 */
	protected EnderContainers plugin;

	/**
	 * Stores the plugin loader
	 */
	protected Logger logger;

	/**
	 * Register a specific listener to the server
	 * @param listener Listener to register
	 */
	protected void registerListener(Listener listener) {
		this.plugin.getServer().getPluginManager().registerEvents(listener, this.plugin);
	}

	/**
	 * Fill up plugin fields inside the object.
	 * @param plugin The main plugin object
	 */
	void setPlugin(EnderContainers plugin) {
		this.plugin = plugin;
		this.logger = this.plugin.getLogger();
	}

	/**
	 * Called when the manager is initializing.
	 */
	protected void initialize() {
		// Not implemented
	}

	/**
	 * Called when the manager is loading.
	 * Called in the constructor so before the constructor of sub-managers.
	 */
	public void load() {
		// Not implemented
	}

	/**
	 * Called when the manager is unloading.
	 */
	protected void unload() {
		// Not implemented
	}

}

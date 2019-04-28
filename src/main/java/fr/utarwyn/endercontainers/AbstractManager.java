package fr.utarwyn.endercontainers;

import org.bukkit.event.Listener;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Abtsract base class for creating an EnderContainers manager
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
	 * Constructs the manager
	 * @param plugin Main class of the plugin
	 * @param listeners List of listeners to automatically load during the initialization of the manager
	 */
	public AbstractManager(EnderContainers plugin, Listener ... listeners) {
		this.plugin = plugin;
		this.logger = this.plugin.getLogger();

		// Now we register all listeners (and the manager too)
		this.registerListener(this);
		for (Listener listener : listeners) {
			this.registerListener(listener);
		}

		// We start the initialization of the manager
		this.load();

		// Register the manager for the plugin
		try {
			Managers.registerManager(this.getClass(), this);
		} catch (Exception e) {
			this.logger.log(Level.SEVERE, "Cannot register the manager " + this.getClass().getName(), e);
		}
	}

	/**
	 * Register a specific listener to the server
	 * @param listener Listener to register
	 */
	protected void registerListener(Listener listener) {
		this.plugin.getServer().getPluginManager().registerEvents(listener, this.plugin);
	}

	/**
	 * Returns the main class of the plugin. Only a shortcut.
	 * @return Main class
	 */
	public EnderContainers getPlugin() {
		return this.plugin;
	}

	/**
	 * Called when the manager is loading.
	 * Called in the constructor so before the constructor of sub-managers.
	 */
	public abstract void load();

	/**
	 * Called when the manager is unloading.
	 */
	protected abstract void unload();

}

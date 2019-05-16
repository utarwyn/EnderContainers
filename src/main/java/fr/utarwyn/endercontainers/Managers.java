package fr.utarwyn.endercontainers;

import java.util.HashMap;
import java.util.logging.Level;

/**
 * Class used to manage managers (manager-ception).
 * @since 2.0.0
 * @author Utarwyn
 */
public class Managers {

	/**
	 * Cache map for instances of managers
	 */
	private static HashMap<Class<? extends AbstractManager>, AbstractManager> instances = new HashMap<>();

	/**
	 * Register a specific manager in the memory
	 * @param clazz Class of the manager to load for the plugin
	 * @return Instance of the registered manager. Null if the manager is already registered.
	 */
	static <T extends AbstractManager> T registerManager(EnderContainers plugin, Class<T> clazz) {
		if (!instances.containsKey(clazz)) {
			try {
				T instance = clazz.newInstance();

				instances.put(clazz, instance);

				instance.setPlugin(plugin);
				instance.initialize();
				instance.load();

				return instance;
			} catch (InstantiationException | IllegalAccessException e) {
				plugin.getLogger().log(Level.SEVERE, "Cannot register the manager " + clazz.getName(), e);
			}
		}

		return null;
	}

	/**
	 * Reload all managers
	 */
	public static void reloadAll() {
		for (AbstractManager manager : instances.values()) {
			manager.unload();
			manager.load();
		}
	}

	/**
	 * Unload all managers
	 */
	static void unloadAll() {
		for (AbstractManager manager : instances.values()) {
			manager.unload();
		}
	}

	/**
	 * Gets an instance of a manager by its class
	 * @param clazz Class of the manager to get
	 * @param <T> Class type of the manager
	 * @return Manager if found otherwise null
	 */
	@SuppressWarnings("unchecked")
	protected static <T extends AbstractManager> T getInstance(Class<T> clazz) {
		AbstractManager instance = instances.get(clazz);

		if (instance == null)
			return null;
		if (clazz.isInstance(instance))
			return (T) instance;

		return null;
	}

	/**
	 * Reload manager by its class
	 * @param managerClazz Class of the manager to reload
	 */
	public static void reload(Class<? extends AbstractManager> managerClazz) {
		if (!instances.containsKey(managerClazz)) return;

		instances.get(managerClazz).unload();
		instances.get(managerClazz).load();
	}

}

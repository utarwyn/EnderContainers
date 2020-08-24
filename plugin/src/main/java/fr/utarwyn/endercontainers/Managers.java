package fr.utarwyn.endercontainers;

import java.util.*;
import java.util.logging.Level;

/**
 * Class used to manage managers.
 *
 * @author Utarwyn
 * @since 2.0.0
 */
public class Managers {

    /**
     * Cache map for instances of managers.
     */
    static Map<Class<? extends AbstractManager>, AbstractManager> instances = new LinkedHashMap<>();

    /**
     * Managers constructor.
     * It's an utility class, it does not have to be instanciated.
     */
    private Managers() {
        // Not implemented
    }

    /**
     * Gets an instance of a manager by its class.
     *
     * @param clazz class of the manager to get
     * @param <T>   class type of the manager
     * @return manager if found otherwise null
     */
    @SuppressWarnings("unchecked")
    public static <T extends AbstractManager> T get(Class<T> clazz) {
        AbstractManager instance = instances.get(clazz);

        if (clazz.isInstance(instance)) {
            return (T) instance;
        } else {
            throw new NullPointerException(clazz + " instance is null!");
        }
    }

    /**
     * Reload manager by its class.
     *
     * @param managerClazz class of the manager to reload
     * @return true if the manager has been reloaded
     */
    public static boolean reload(Class<? extends AbstractManager> managerClazz) {
        if (instances.containsKey(managerClazz)) {
            AbstractManager manager = instances.get(managerClazz);

            manager.unload();
            manager.load();
            return true;
        }

        return false;
    }

    /**
     * Unload all managers and load them.
     */
    public static void reloadAll() {
        Managers.unloadAll();
        instances.values().forEach(AbstractManager::load);
    }

    /**
     * Register a specific manager in the memory.
     *
     * @param clazz class of the manager to load for the plugin
     * @return instance of the registered manager. Null if the manager is already registered.
     */
    static <T extends AbstractManager> T register(EnderContainers plugin, Class<T> clazz) {
        if (!instances.containsKey(clazz)) {
            try {
                T instance = clazz.getDeclaredConstructor().newInstance();

                instances.put(clazz, instance);

                instance.setPlugin(plugin);
                instance.initialize();
                instance.load();

                return instance;
            } catch (ReflectiveOperationException e) {
                plugin.getLogger().log(Level.SEVERE,
                        String.format("Cannot register the manager %s", clazz.getName()), e);
            }
        }

        return null;
    }

    /**
     * Unload all managers in a reverse order.
     */
    static void unloadAll() {
        List<AbstractManager> managers = new ArrayList<>(instances.values());
        Collections.reverse(managers);
        managers.forEach(AbstractManager::unload);
    }

    /**
     * Clears the cache with all managers instances.
     */
    static void clear() {
        instances.clear();
    }

}

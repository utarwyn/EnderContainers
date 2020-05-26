package fr.utarwyn.endercontainers.configuration;

import fr.utarwyn.endercontainers.configuration.wrapper.YamlFileLoadException;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * A file handler which create and contains
 * a single instance of all config files.
 *
 * @author Utarwyn <maxime.malgorn@laposte.net>
 * @since 2.2.0
 */
public class Files {

    /**
     * The singleton instance for the main configuration
     */
    private static Configuration configuration;

    /**
     * The singleton instance for the locale file!
     */
    private static Locale locale;

    /**
     * This class cannot be instancied, it just contains config file instances.
     */
    private Files() {
        // Not implemented
    }

    /**
     * Gets the global plugin configuration. Its a singleton through all the plugin.
     *
     * @return The plugin's configuration
     */
    public static Configuration getConfiguration() {
        return configuration;
    }

    /**
     * Gets the global plugin locale. Its a singleton through all the plugin.
     *
     * @return The plugin's locale
     */
    public static Locale getLocale() {
        return locale;
    }

    /**
     * Constructs a new configuration instance.
     * If the instance exists, it does not create a new one.
     *
     * @param plugin java plugin object
     * @throws YamlFileLoadException thrown if the configuration file cannot be loaded
     */
    public static void initConfiguration(JavaPlugin plugin) throws YamlFileLoadException {
        if (configuration == null) {
            configuration = new Configuration(plugin);
            configuration.load();
        }
    }

    /**
     * Constructs a new locale instance.
     * If the instance exists, it does not create a new one.
     *
     * @param plugin java plugin object
     * @throws YamlFileLoadException thrown if the configuration file cannot be loaded
     */
    public static void initLocale(JavaPlugin plugin) throws YamlFileLoadException {
        if (locale == null) {
            locale = new Locale(plugin);
            locale.load();
        }
    }

}

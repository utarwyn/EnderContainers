package fr.utarwyn.endercontainers.configuration;

import org.bukkit.plugin.Plugin;

/**
 * Manages all configurations of the plugin.
 * Currently handles messages and global configs.
 *
 * @author Utarwyn
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
     * Initialize all configuration files that the plugin uses.
     *
     * @param plugin java plugin object
     * @throws ConfigLoadingException thrown if one configuration file cannot be loaded
     */
    public static void reload(Plugin plugin) throws ConfigLoadingException {
        configuration = new Configuration(plugin);
        locale = new Locale(plugin, configuration.getLocale());
    }

}

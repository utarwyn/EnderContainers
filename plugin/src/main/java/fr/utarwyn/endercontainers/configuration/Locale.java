package fr.utarwyn.endercontainers.configuration;

import fr.utarwyn.endercontainers.compatibility.ServerVersion;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

/**
 * Locale class. Reflects a locale .yml file.
 *
 * @author Utarwyn
 * @since 2.0.0
 */
public class Locale {

    /**
     * Locale will be loaded from file with that name
     */
    private static final String CUSTOM = "custom";

    /**
     * Java plugin instance
     */
    private final Plugin plugin;

    /**
     * Internal path for locale configuration in plugin resources
     */
    private final String resource;

    /**
     * File object which stores custom locale if needed
     */
    private final File file;

    /**
     * Cache map with all messages retrieved from the configuration
     */
    private final Map<String, String> cache;

    /**
     * Locale configuration object
     */
    private FileConfiguration configuration;

    /**
     * Constructs the locale object.
     *
     * @param plugin java plugin object
     * @param locale locale to load based on plugin configuration
     * @throws ConfigLoadingException thrown if cannot load locale file
     */
    Locale(Plugin plugin, String locale) throws ConfigLoadingException {
        this.plugin = plugin;
        this.cache = new HashMap<>();

        // If locale is handled as custom, load locale file from disk first
        if (CUSTOM.equals(locale)) {
            this.file = new File(plugin.getDataFolder(), "locale.yml");
            locale = "en";
        } else {
            this.file = null;
        }

        // Also define internal resource file which will be the default one
        this.resource = String.format("locales/%s.yml", locale);

        this.load();
    }

    /**
     * Retreive a message from the locale file by its key.
     *
     * @param key localization key
     * @return retreived message from the loaded file
     */
    public String getMessage(LocaleKey key) {
        return this.cache.computeIfAbsent(key.getKey(), k ->
                this.formatMessage(this.configuration.getString(k)));
    }

    /**
     * Loads the locale from configuration file.
     *
     * @throws ConfigLoadingException thrown if cannot load locale file
     */
    private void load() throws ConfigLoadingException {
        FileConfiguration defaults = this.loadFromStream(resource);

        if (file != null && file.exists()) {
            this.configuration = YamlConfiguration.loadConfiguration(file);
            this.configuration.setDefaults(defaults);
        } else {
            this.configuration = defaults;
            this.configuration.options().copyDefaults(true);
            this.saveToFileIfNotExist(resource, file);
        }
    }

    /**
     * Formats a message from the locale configuration file before using it.
     *
     * @param message message to format
     * @return formatted message from the configuration
     */
    private String formatMessage(String message) {
        if (message == null) return null;

        if (ServerVersion.isOlderThan(ServerVersion.V1_9)) {
            message = new String(message.getBytes(), StandardCharsets.UTF_8);
        }

        return ChatColor.translateAlternateColorCodes('&', message);
    }

    /**
     * Loads configuration from an internal resource file.
     *
     * @param resource path to the resource file to be loaded
     * @return configuration object based on the resource file
     * @throws ConfigLoadingException thrown if cannot read the resource file
     */
    private FileConfiguration loadFromStream(String resource) throws ConfigLoadingException {
        InputStream stream = this.plugin.getResource(resource);
        if (stream != null) {
            try (InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
                return YamlConfiguration.loadConfiguration(reader);
            } catch (IOException e) {
                throw new ConfigLoadingException("cannot read internal locale file " + this.resource, e);
            }
        } else {
            throw new ConfigLoadingException("cannot read internal locale file " + this.resource);
        }
    }

    /**
     * Saves a configuration on the disk if do not exist.
     *
     * @param resource path to the internal resource file
     * @param file     file to save configuration to
     * @throws ConfigLoadingException thrown if cannot save the file (io error)
     */
    private void saveToFileIfNotExist(String resource, File file) throws ConfigLoadingException {
        InputStream stream = this.plugin.getResource(resource);
        if (stream != null && file != null && !file.exists()) {
            try {
                Files.copy(stream, file.toPath());
                this.plugin.getLogger().log(Level.INFO, "Created custom locale file at {0}", file.getPath());
            } catch (IOException e) {
                throw new ConfigLoadingException("cannot save configuration file", e);
            }
        }
    }

}

package fr.utarwyn.endercontainers.configuration;

import fr.utarwyn.endercontainers.compatibility.ServerVersion;
import fr.utarwyn.endercontainers.configuration.wrapper.YamlFileLoadException;
import fr.utarwyn.endercontainers.configuration.wrapper.YamlFileWrapper;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Locale class. Reflects a locale .yml file.
 *
 * @author Utarwyn
 * @since 2.0.0
 */
public class Locale extends YamlFileWrapper {

    /**
     * Cache map with all messages retrieved from the configuration
     */
    private final Map<String, String> cache;

    /**
     * Constructs the locale object.
     *
     * @param plugin java plugin object
     */
    Locale(JavaPlugin plugin) {
        super(
                new File(plugin.getDataFolder(), "locale.yml"),
                plugin.getClass().getResource("/locales/en.yml")
        );

        this.cache = new HashMap<>();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void load() throws YamlFileLoadException {
        super.load();
        this.cache.clear();
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
     * Format a message from the locale configuration file before using it.
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

}

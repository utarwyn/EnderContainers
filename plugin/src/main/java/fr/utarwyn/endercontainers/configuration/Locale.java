package fr.utarwyn.endercontainers.configuration;

import fr.utarwyn.endercontainers.compatibility.ServerVersion;
import org.bukkit.ChatColor;
import org.bukkit.configuration.Configuration;
import org.bukkit.plugin.java.JavaPlugin;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Locale class. Reflects a locale .yml file.
 *
 * @author Utarwyn
 * @since 2.0.0
 */
public class Locale extends YamlFile {

    /**
     * Bukkit configuration object which contains all messages
     */
    private Configuration configuration;

    /**
     * Localized message cache
     */
    private Map<String, String> cache;

    Locale(JavaPlugin plugin) {
        super(plugin, "locale.yml");

        this.cache = new HashMap<>();
        this.setDefaultFilename("locales/en.yml");
    }

    /**
     * Retreive a message from the locale file by its key.
     *
     * @param key localization key
     * @return retreived message from the loaded file
     */
    public String getMessage(LocaleKey key) {
        return this.cache.computeIfAbsent(key.getKey(), k ->
                (String) this.parseValue(k, this.configuration.getString(k)));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean load() {
        this.configuration = this.getFileConfiguration();
        return this.configuration != null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Object parseValue(String key, Object value) {
        if (value instanceof String) {
            String message = String.valueOf(value);

            if (ServerVersion.isOlderThan(ServerVersion.V1_9)) {
                message = new String(message.getBytes(), StandardCharsets.UTF_8);
            }

            return ChatColor.translateAlternateColorCodes('&', message);
        }

        return value;
    }

}

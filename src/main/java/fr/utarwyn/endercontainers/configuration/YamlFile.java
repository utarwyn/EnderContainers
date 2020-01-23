package fr.utarwyn.endercontainers.configuration;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.logging.Level;

/**
 * Class to access to all data from a YAML file.
 * It allows us to bind class attributes to a Yaml key and load the value directly.
 *
 * @author Utarwyn
 * @link https://en.wikipedia.org/wiki/YAML
 * @since 2.0.0
 */
public abstract class YamlFile {

    /**
     * The Endercontainers plugin object
     */
    protected JavaPlugin plugin;

    /**
     * Java file object
     */
    private File file;

    /**
     * Name of the file used to aggregate the editable configuration
     */
    private String defaultFilename;

    /**
     * Construct a new file object written with YAML language.
     *
     * @param plugin   the bukkit plugin
     * @param filename name of the resource to manage with the instance
     */
    YamlFile(JavaPlugin plugin, String filename) {
        this.plugin = plugin;

        if (filename != null) {
            this.file = new File(plugin.getDataFolder(), filename);
            this.defaultFilename = this.file.getName();
        }
    }

    /**
     * Sets the name of the default internal configuration file for this instance.
     *
     * @param defaultFilename default filename
     */
    public void setDefaultFilename(String defaultFilename) {
        this.defaultFilename = defaultFilename;
    }

    /**
     * Fill all configurable attributes in this class with
     * the {@link Configurable Configurable} annotation and load
     * their values from the configuration file.
     *
     * @return True if all configuration values have been loaded.
     */
    public boolean load() {
        // Gets the configuration file
        FileConfiguration configuration = this.getFileConfiguration();

        // Load every needed config value dynamically!
        for (Field field : this.getClass().getDeclaredFields()) {
            Configurable conf = field.getAnnotation(Configurable.class);
            if (conf == null) continue;

            // Getting the config key associated with the field
            String configKey = (conf.key().isEmpty()) ? field.getName() : conf.key();
            Object value = configuration.get(configKey);

            // Changing the value of the field
            try {
                field.setAccessible(true);
                field.set(this, this.parseValue(configKey, value));
                field.setAccessible(false);
            } catch (ReflectiveOperationException | IllegalArgumentException e) {
                String configName = getClass().getSimpleName().toLowerCase();
                String message = String.format("Cannot parse the config key '%s' in %s file", configKey, configName);

                this.plugin.getLogger().log(Level.SEVERE, message, e);
                this.plugin.getPluginLoader().disablePlugin(this.plugin);

                return false;
            }
        }

        return true;
    }

    /**
     * Reload the configuration from the Plugin config file.
     *
     * @return True if the reload was a success.
     */
    public boolean reload() {
        return load();
    }

    /**
     * Parses a value loaded from the Yaml file.
     * The method does nothing by default.
     *
     * @param key   Key linked to the value
     * @param value The value to parse
     * @return Parsed value. Returns the same value by default.
     */
    protected Object parseValue(String key, Object value) {
        return value;
    }

    /**
     * Gets a file configuration object with default values.
     * If the editable file has not already been created, this method will create it.
     *
     * @return configuration object ready to be parsed
     */
    protected FileConfiguration getFileConfiguration() {
        InputStream defConfigStream = this.plugin.getResource(this.defaultFilename);

        // Create the file from the internal template if does not exist in the data folder
        if (!this.file.exists()) {
            if (defConfigStream != null) {
                try {
                    Files.copy(defConfigStream, this.file.toPath());
                } catch (IOException e) {
                    return null;
                }
            } else {
                throw new NullPointerException("Cannot find the default configuration " + this.defaultFilename);
            }
        }

        // Load the configuration from the file and set the default configuration if provided
        FileConfiguration configuration = YamlConfiguration.loadConfiguration(this.file);

        if (defConfigStream != null) {
            configuration.setDefaults(YamlConfiguration.loadConfiguration(new InputStreamReader(defConfigStream, StandardCharsets.UTF_8)));
        }

        return configuration;
    }

}

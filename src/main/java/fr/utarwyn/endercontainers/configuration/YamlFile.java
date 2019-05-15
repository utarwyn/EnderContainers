package fr.utarwyn.endercontainers.configuration;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Field;
import java.util.logging.Level;

/**
 * Class to access to all data from a YAML file.
 * It allows us to bind class attributes to a Yaml key and load the value directly.
 *
 * @link https://en.wikipedia.org/wiki/YAML
 * @since 2.0.0
 * @author Utarwyn
 */
public abstract class YamlFile {

	/**
	 * The Endercontainers plugin object
	 */
	protected JavaPlugin plugin;

	/**
	 * Construct a YamlFile.
	 * It shoulds only exist once during the plugin lifecycle.
	 * @param plugin The EnderContainers Java plugin
	 */
	YamlFile(JavaPlugin plugin) {
		this.plugin = plugin;
	}

	/**
	 * Fill all configurable attributes in this class with
	 * the {@link Configurable Configurable} annotation.
	 * @return True if all configuration values has been loaded.
	 */
	public boolean load() {
		// Gets the configuration file from
		FileConfiguration configuration = this.getFileConfiguration();

		// Load every needed config value dynamically!
		for (Field field : this.getClass().getDeclaredFields()) {
			Configurable conf = field.getAnnotation(Configurable.class);
			if (conf == null) continue;

			// Getting the config key associated with the field
			String configKey = (conf.key().isEmpty()) ? field.getName() : conf.key();

			// Changing the value of the field
			try {
				Object value = configuration.get(configKey);
				if (value == null) {
					throw new NullPointerException("Yaml value doesn't exist for key " + configKey + "!");
				}

				field.setAccessible(true);
				field.set(this, parseValue(configKey, value));
				field.setAccessible(false);
			} catch (Exception e) {
				plugin.getLogger().severe("");
				plugin.getLogger().severe(">> --------------- <<");
				plugin.getLogger().severe(">> CRITICAL ERROR! <<");
				plugin.getLogger().severe(">> --------------- <<");
				plugin.getLogger().severe("");
				plugin.getLogger().severe(">> Error when loading Yaml for class " + this.getClass().getSimpleName() + "! Yaml value " + configKey.toUpperCase() + " cannot be parsed!");
				plugin.getLogger().severe(">> Are you sure that value '" + configuration.get(configKey) + "' is good?");
				plugin.getLogger().severe("");

				plugin.getLogger().log(Level.SEVERE, "Cannot parse the yaml file", e);

				this.plugin.getPluginLoader().disablePlugin(this.plugin);
				return false;
			}
		}

		return true;
	}

	/**
	 * Reload the configuration from the Plugin config file.
	 * @return True if the reload was a success.
	 */
	public boolean reload() {
		this.plugin.reloadConfig();
		return load();
	}

	/**
	 * Parses a value loaded from the Yaml file.
	 * The method does nothing by default.
	 *
	 * @param key Key linked to the value
	 * @param value The value to parse
	 * @return Parsed value. Returns the same value by default.
	 */
	protected Object parseValue(String key, Object value) {
		return value;
	}

	/**
	 * This method have to return the configuration to be loaded in this class instance.
	 * @return The file configuration object
	 */
	protected abstract FileConfiguration getFileConfiguration();

}

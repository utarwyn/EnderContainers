package fr.utarwyn.endercontainers.util;

import fr.utarwyn.endercontainers.EnderContainers;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Field;

/**
 * Represents a link to a particular Yaml config.
 * It allows to load all Yaml data into a Java object easily.
 * <p>
 * With this class, we have to create an attribute
 * for each Yaml key and the value will be loaded inside it.
 * </p>
 *
 * @since 2.0.0
 * @author Utarwyn
 */
public abstract class YamlLinker {

	/**
	 * Initialize the config from the file.
	 * @param plugin Bukkit plugin used to locate the config file.
	 * @return True if the initialization phase succeed.
	 */
	public boolean initialize(JavaPlugin plugin) {
		return false;
	}

	/**
	 * Reload the configuration from the Plugin config file.
	 * @return True if the reload was a success.
	 */
	public boolean reload() {
		JavaPlugin plugin = EnderContainers.getInstance();

		plugin.reloadConfig();
		return initialize(plugin);
	}

	/**
	 * Parses a value loaded from the Yaml file.
	 * The method do nothing by default.
	 *
	 * @param key Key linked to the value
	 * @param value The value to parse
	 * @return Parsed value. Returns the same value by default.
	 */
	protected Object parseValue(String key, Object value) {
		return value;
	}

	/**
	 * Fill all configurable attributes in this class with the {@link fr.utarwyn.endercontainers.util.Configurable Configurable} annotation.
	 * @param configuration Configuration used to load all Yaml key/values.
	 * @return True if all configuration values has been loaded.
	 */
	protected boolean load(FileConfiguration configuration) {
		// Load every needed config value dynamically!
		for (Field field : this.getClass().getDeclaredFields()) {
			Configurable conf = field.getAnnotation(Configurable.class);
			if (conf == null) continue;

			// Getting the config key associated with the field
			String configKey = (conf.key().isEmpty()) ? field.getName() : conf.key();

			// Changing the value of the field
			try {
				Object value = configuration.get(configKey);
				if (value == null)
					throw new NullPointerException("Yaml value doesn't exist for key " + configKey + "!");

				field.set(null, parseValue(configKey, value));
			} catch (Exception e) {
				Log.error("");
				Log.error(">> --------------- <<");
				Log.error(">> CRITICAL ERROR! <<");
				Log.error(">> --------------- <<");
				Log.error("");
				Log.error(">> Error when loading Yaml for class " + this.getClass().getSimpleName() + "! Yaml value " + configKey.toUpperCase() + " cannot be parsed!");
				Log.error(">> Are you sure that value '" + configuration.get(configKey) + "' is good?");
				Log.error("");

				e.printStackTrace();

				EnderContainers.getInstance().getPluginLoader().disablePlugin(EnderContainers.getInstance());
				return false;
			}
		}

		return true;
	}

}

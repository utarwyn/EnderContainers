package fr.utarwyn.endercontainers;

import fr.utarwyn.endercontainers.util.Configurable;
import fr.utarwyn.endercontainers.util.Log;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.lang.reflect.Field;
import java.util.List;

/**
 * Configuration class. Reflets the config.yml
 * @since 2.0.0
 * @author Utarwyn
 */
public class Config {

	/**
	 * No constructor, its an utility class
	 */
	private Config() {}

	/**
	 * Download link of the plugin
	 */
	public static final String DOWNLOAD_LINK = "http://bit.ly/2A8Xv8S";

	@Configurable
	public static boolean enabled;

	@Configurable
	public static boolean debug;

	@Configurable
	public static String prefix;

	@Configurable
	public static String locale;

	@Configurable
	public static List<String> disabledWorlds;

	@Configurable(key = "enderchests.max")
	public static Integer maxEnderchests;

	@Configurable(key = "enderchests.default")
	public static Integer defaultEnderchests;

	@Configurable(key = "enderchests.onlyShowAccessible")
	public static boolean onlyShowAccessibleEnderchests;

	@Configurable(key = "enderchests.useVanillaEnderchest")
	public static boolean useVanillaEnderchest;

	@Configurable(key = "mysql.enabled")
	public static boolean mysql;

	@Configurable(key = "mysql.host")
	public static String mysqlHost;

	@Configurable(key = "mysql.port")
	public static int mysqlPort;

	@Configurable(key = "mysql.user")
	public static String mysqlUser;

	@Configurable(key = "mysql.password")
	public static String mysqlPassword;

	@Configurable(key = "mysql.database")
	public static String mysqlDatabase;

	@Configurable(key = "mysql.tablePrefix")
	public static String mysqlTablePrefix;

	@Configurable(key = "others.blockNametag")
	public static boolean blockNametag;

	@Configurable(key = "others.updateChecker")
	public static boolean updateChecker;

	/**
	 * Initialize the config from the file.
	 * @param plugin Bukkit plugin used to locate the config file.
	 * @return True if the initialization phase succeed.
	 */
	static boolean initialize(JavaPlugin plugin) {
		// Create config.yml file if not exists
		if (!new File(plugin.getDataFolder(), "config.yml").exists())
			plugin.saveDefaultConfig();

		// And load all config values ...
		return load(plugin.getConfig());
	}

	/**
	 * Reload the configuration from the Plugin config file.
	 * @return True if the reload was a success.
	 */
	public static boolean reload() {
		JavaPlugin plugin = EnderContainers.getInstance();

		plugin.reloadConfig();
		return initialize(plugin);
	}

	/**
	 * Fill all config attributes in this class with the {@link fr.utarwyn.endercontainers.util.Configurable Configurable} annotation.
	 * @param pluginConfiguration Configuration used to load all config key/values.
	 * @return True if all configuration values has been loaded.
	 */
	private static boolean load(FileConfiguration pluginConfiguration) {
		// Load every needed config value dynamically!
		for (Field field : Config.class.getDeclaredFields()) {
			Configurable conf = field.getAnnotation(Configurable.class);
			if (conf == null) continue;

			// Getting the config key associated with the field
			String configKey = (conf.key().isEmpty()) ? field.getName() : conf.key();

			// Changing the value of the field
			try {
				field.set(null, pluginConfiguration.get(configKey));
			} catch (Exception e) {
				Log.error("");
				Log.error(">> --------------- <<");
				Log.error(">> CRITICAL ERROR! <<");
				Log.error(">> --------------- <<");
				Log.error("");
				Log.error(">> Error when loading config! Config value " + configKey.toUpperCase() + " cannot be parsed!");
				Log.error(">> Are you sure that value '" + pluginConfiguration.get(configKey) + "' is good?");
				Log.error("");

				e.printStackTrace();

				EnderContainers.getInstance().getPluginLoader().disablePlugin(EnderContainers.getInstance());
				return false;
			}
		}

		return true;
	}

}

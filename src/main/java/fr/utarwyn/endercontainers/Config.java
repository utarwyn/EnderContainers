package fr.utarwyn.endercontainers;

import fr.utarwyn.endercontainers.util.Configurable;
import fr.utarwyn.endercontainers.util.YamlLinker;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.List;

/**
 * Configuration class. Reflets the config.yml
 * @since 2.0.0
 * @author Utarwyn
 */
public class Config extends YamlLinker {

	/**
	 * The config static instance
	 */
	private static Config instance;

	/**
	 * No constructor, its an utility class
	 */
	private Config() {}

	/**
	 * Download link of the plugin
	 */
	public static final String DOWNLOAD_LINK = "http://bit.ly/2A8Xv8S";

	/**
	 * The plugin prefix
	 */
	public static final String PREFIX = "§8[§6EnderContainers§8] §7";

	@Configurable
	public static boolean enabled;

	@Configurable
	public static boolean debug;

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

	@Configurable(key = "others.useExperimentalSavingSystem")
	public static boolean useExperimentalSavingSystem;

	public boolean initialize(JavaPlugin plugin) {
		// Create config.yml file if not exists
		if (!new File(plugin.getDataFolder(), "config.yml").exists())
			plugin.saveDefaultConfig();

		// And load all config values ...
		return this.load(plugin.getConfig());
	}

	/**
	 * Gets the Config instance from anywhere!
	 * (Create it if it don't exists)
	 * @return The config instance.
	 */
	public static Config get() {
		if (instance != null) return instance;
		return instance = new Config();
	}

}

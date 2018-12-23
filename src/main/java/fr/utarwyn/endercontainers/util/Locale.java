package fr.utarwyn.endercontainers.util;

import fr.utarwyn.endercontainers.Config;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.nio.charset.Charset;

/**
 * Locale class. Reflets the custom locale .yml file
 * @since 2.0.0
 * @author Utarwyn
 */
public class Locale extends YamlLinker {

	/**
	 * The Locale static instance
	 */
	private static Locale instance;

	/**
	 * No constructor, its an utility class
	 */
	private Locale() {

	}

	@Configurable(key = "commands.backups.created")
	public static String backupCreated;

	@Configurable(key = "commands.backups.creation_starting")
	public static String backupCreationStarting;

	@Configurable(key = "commands.backups.exists")
	public static String backupExists;

	@Configurable(key = "commands.backups.info")
	public static String backupInfo;

	@Configurable(key = "commands.backups.loaded")
	public static String backupLoaded;

	@Configurable(key = "commands.backups.loading_starting")
	public static String backupLoadingStarted;

	@Configurable(key = "commands.backups.removed")
	public static String backupRemoved;

	@Configurable(key = "commands.backups.unknown")
	public static String backupUnknown;

	@Configurable(key = "commands.backups.zero")
	public static String backupZero;

	@Configurable(key = "commands.backups.label_name")
	public static String backupLabelName;

	@Configurable(key = "commands.backups.label_date")
	public static String backupLabelDate;

	@Configurable(key = "commands.backups.label_by")
	public static String backupLabelBy;

	@Configurable(key = "commands.backups.label_loadcmd")
	public static String backupLabelLoadCmd;

	@Configurable(key = "commands.backups.label_rmcmd")
	public static String backupLabelRmCmd;

	@Configurable(key = "commands.config_reloaded")
	public static String configReloaded;

	@Configurable(key = "commands.no_update")
	public static String noUpdate;

	@Configurable(key = "menus.main_title")
	public static String menuMainTitle;

	@Configurable(key = "menus.chest_title")
	public static String menuChestTitle;

	@Configurable(key = "menus.pane_title")
	public static String menuPaneTitle;

	@Configurable(key = "menus.chest_empty")
	public static String menuChestEmpty;

	@Configurable(key = "menus.chest_full")
	public static String menuChestFull;

	@Configurable(key = "menus.chest_locked")
	public static String menuChestLocked;

	@Configurable(key = "menus.previous_page")
	public static String menuPreviousPage;

	@Configurable(key = "menus.next_page")
	public static String menuNextPage;

	@Configurable(key = "errors.noperm_open_chest")
	public static String nopermOpenChest;

	@Configurable(key = "errors.noperm_console")
	public static String nopermConsole;

	@Configurable(key = "errors.noperm_player")
	public static String nopermPlayer;

	@Configurable(key = "errors.plugin_disabled")
	public static String pluginDisabled;

	@Configurable(key = "errors.plugin_world_disabled")
	public static String pluginWorldDisabled;

	@Configurable(key = "errors.cmd_invalid_parameter")
	public static String cmdInvalidParameter;

	@Configurable(key = "errors.cmd_wrong_argument_count")
	public static String cmdWrongArgumentCount;

	@Configurable(key = "dependencies.access_denied_factions")
	public static String accessDeniedFactions;

	@Configurable(key = "dependencies.access_denied_plotsq")
	public static String accessDeniedPlotSq;

	@Configurable(key = "miscellaneous.chest_nametag")
	public static String chestNametag;

	@Override
	protected Object parseValue(String key, Object value) {
		if (value instanceof String) {
			String bukkitVersion = EUtil.getServerVersion();
			String message = String.valueOf(value);

			if (bukkitVersion.contains("v1_7") || bukkitVersion.contains("v1_8"))
				message = new String(message.getBytes(), Charset.forName("UTF-8"));

			return ChatColor.translateAlternateColorCodes('&', message);
		}

		return value;
	}

	/**
	 * Initialize the locale from the custom file.
	 * @param plugin Bukkit plugin used to locate and init the locale file.
	 * @return True if the initialization phase succeed.
	 */
	public boolean initialize(JavaPlugin plugin) {
		// Create custom locale .yml file if not exists
		File localeFolder = new File(plugin.getDataFolder(), "locales/");
		File file = new File(localeFolder, Config.locale + ".yml");

		if (!file.exists()) {
			// File doesn't exists... use the template to pre-fill the data.
			try {
				if (!localeFolder.exists() && !localeFolder.mkdir()) return false;
				if (!file.createNewFile()) return false;

				InputStream in = plugin.getResource("locale.yml");
				InputStreamReader isr = new InputStreamReader(in, "UTF8");

				FileOutputStream out = new FileOutputStream(file);
				OutputStreamWriter osw = new OutputStreamWriter(out, "UTF8");

				char[] tempbytes = new char[512];
				int readbytes = isr.read(tempbytes, 0, 512);

				while (readbytes > -1) {
					osw.write(tempbytes, 0, readbytes);
					readbytes = isr.read(tempbytes, 0, 512);
				}

				osw.close();
				isr.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		// And load all locale messages ...
		return this.load(YamlConfiguration.loadConfiguration(file));
	}

	/**
	 * Gets the Config instance from anywhere!
	 * (Create it if it don't exists)
	 * @return The config instance.
	 */
	public static Locale get() {
		if (instance != null) return instance;
		return instance = new Locale();
	}

}

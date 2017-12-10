package fr.utarwyn.endercontainers.util;

import fr.utarwyn.endercontainers.AbstractManager;
import fr.utarwyn.endercontainers.Config;
import fr.utarwyn.endercontainers.EnderContainers;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.*;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

/**
 * Manage locales for all messages of the plugin
 * @since 2.0.0
 * @author Utarwyn
 */
public class LocaleManager extends AbstractManager {

	/**
	 * Instance of the manager, used to create a static shortcut
	 */
	private static LocaleManager instance;

	/**
	 * Stores all messages key -> value
	 */
	private Map<String, String> messages;

	/**
	 * Constructs the manager instance and initialize attributes
	 */
	public LocaleManager() {
		super(EnderContainers.getInstance());
		instance = this;
	}

	/**
	 * Initializes the manager
	 */
	@Override
	public void initialize() {
		this.messages = new HashMap<>();
		File file = new File(this.getPlugin().getDataFolder(), "locales/" + Config.locale + ".yml");

		// File doesn't exists... use the template to pre-fill the data.
		if (!file.exists()) {
			try {
				if (!file.getParentFile().mkdir()) return;
				if (!file.createNewFile()) return;

				InputStream in = this.getPlugin().getResource("locale.yml");
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

		YamlConfiguration config = YamlConfiguration.loadConfiguration(file);

		for (String key : config.getKeys(false))
			messages.put(key, config.getString(key));
	}

	/**
	 * Unloads the manager
	 */
	@Override
	protected void unload() {

	}

	/**
	 * Gets a locale value for a given key
	 * @param key Key where to find the message
	 * @return Message stored by the given key
	 */
	private String get(String key) {
		String bukkitVersion = EUtil.getServerVersion();
		String message = messages.get(key);

		if (bukkitVersion.contains("v1_7") || bukkitVersion.contains("v1_8"))
			message = new String(messages.get(key).getBytes(), Charset.forName("UTF-8"));

		if (!messages.containsKey(key))
			throw new IllegalArgumentException("Locale key '" + key + "' does not exists in the locale file!");

		return ChatColor.translateAlternateColorCodes('&', message);
	}

	/**
	 * Shortcut used to get a locale message by its key
	 * @param key Key used to search the message
	 * @return The locale message
	 */
	public static String __(String key) {
		return instance.get(key);
	}

}

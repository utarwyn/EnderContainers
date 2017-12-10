package fr.utarwyn.endercontainers.storage;

import fr.utarwyn.endercontainers.EnderContainers;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

/**
 * Manages a flat file stored on the disk.
 * @since 2.0.0
 * @author Utarwyn
 */
public class FlatFile {

	/**
	 * The file object
	 */
	private File file;

	/**
	 * The Bukkit configuration object
	 */
	private YamlConfiguration configuration;

	/**
	 * Constructs a new flat file object
	 * @param path Path where the config file is located (under the plugin's data folder)
	 */
	public FlatFile(String path) {
		this.load(path);
	}

	/**
	 * Returns the configuration object
	 * @return Bukkit configuration object
	 */
	public YamlConfiguration getConfiguration() {
		return this.configuration;
	}

	/**
	 * Save the configuration in memory into the flat file on the disk
	 */
	public void save() {
		if (this.configuration == null || this.file == null)
			throw new NullPointerException("File or configuration seems to be null!");

		try {
			this.configuration.save(this.file);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Load a .yml file into memory and load the configuration object
	 * @param path Path where the config file is located (under the plugin's data folder)
	 */
	private void load(String path) {
		this.file = new File(EnderContainers.getInstance().getDataFolder(), path);

		// Create the flat configuration file if doesn't exists.
		if (!file.exists()) {
			if (!file.getParentFile().exists())
				if (!file.getParentFile().mkdirs())
					return;

			try {
				if (!file.createNewFile()) return;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		this.configuration = YamlConfiguration.loadConfiguration(this.file);
	}

}

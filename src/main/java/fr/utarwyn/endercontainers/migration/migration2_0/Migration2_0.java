package fr.utarwyn.endercontainers.migration.migration2_0;

import fr.utarwyn.endercontainers.EnderContainers;
import fr.utarwyn.endercontainers.migration.Migration;
import fr.utarwyn.endercontainers.migration.YamlNewConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

/**
 * Represents the {@link Migration} between 1.X and 2.X versions.
 * It's a very big migration which managers config, chests and backups data.
 * This migration is separated for both MySQL and flatfile storages.
 *
 * @since 2.0.0
 * @author Utarwyn
 */
public abstract class Migration2_0 extends Migration {

	Migration2_0() {
		super("1.*", "2.*");
	}

	/**
	 * Gets the current version of stored data
	 * @return Version of stored data (for EnderContainers)
	 */
	@Override
	protected String getDataVersion() {
		String saveVersion = EnderContainers.getInstance().getConfig().getString("saveVersion");
		return saveVersion != null ? saveVersion : "2.X";
	}

	/**
	 * Updates the configuration with the old configuration.
	 * This method keeps configuration comments in the Yaml file!
	 * @return True if the configuration has been updated
	 */
	boolean updateConfiguration() {
		File confFile = new File(this.getDataFolder(), "config.yml");
		YamlConfiguration config = YamlConfiguration.loadConfiguration(confFile);

		EnderContainers.getInstance().saveResource("config.yml", true);
		YamlNewConfiguration newConfig = YamlNewConfiguration.loadConfiguration(confFile);

		newConfig.applyConfiguration(config);

		try {
			newConfig.save(confFile);
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}

		return true;
	}

	/**
	 * Updates locales of the plugin.
	 * @return True if locales have been updated
	 */
	boolean updateLocales() {
		// TODO
		return false;
	}

	/**
	 * Reconfigures backups to supports 2.X versions
	 * @return True if backups have been reconfigured
	 */
	abstract boolean reconfigureBackups();

	/**
	 * Reconfigures all enderchests to supports 2.X versions
	 * @return True if all enderchests have been reconfigured
	 */
	abstract boolean reconfigureEnderchests();

}

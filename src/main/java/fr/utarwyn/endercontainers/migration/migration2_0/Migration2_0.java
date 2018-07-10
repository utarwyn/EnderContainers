package fr.utarwyn.endercontainers.migration.migration2_0;

import com.google.common.base.Charsets;
import fr.utarwyn.endercontainers.EnderContainers;
import fr.utarwyn.endercontainers.migration.Migration;
import fr.utarwyn.endercontainers.migration.YamlNewConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents the {@link Migration} between 1.X and 2.X versions.
 * It's a very big migration which managers config, chests and backups data.
 * This migration is separated for both MySQL and flatfile storages.
 *
 * @since 2.0.0
 * @author Utarwyn
 */
public abstract class Migration2_0 extends Migration {

	/**
	 * Links old keys and news keys for locales migration
	 */
	private static final Map<String, String> LOCALE_LINK_MAP = new HashMap<String, String>() {{
		put("cmd_backup_created", "commands.backups.created");
		put("cmd_backup_creation_starting", "commands.backups.creation_starting");
		put("cmd_backup_exists_error", "commands.backups.exists");
		put("cmd_backup_info", "commands.backups.info");
		put("cmd_backup_loaded", "commands.backups.loaded");
		put("cmd_backup_loading_starting", "commands.backups.loading_starting");
		put("cmd_backup_removed", "commands.backups.removed");
		put("cmd_nobackup", "commands.backups.zero");
		put("cmd_backup_label_name", "commands.backups.label_name");
		put("cmd_backup_label_creationdate", "commands.backups.label_date");
		put("cmd_backup_label_createdby", "commands.backups.label_by");
		put("cmd_backup_label_loadcmd", "commands.backups.label_loadcmd");
		put("cmd_backup_label_removecmd", "commands.backups.label_rmcmd");
		put("cmd_config_reloaded", "commands.config_reloaded");
		put("cmd_update_notfound", "commands.no_update");
		put("cmd_unknown_error", "commands.unknown");

		put("enderchest_main_gui_title", "menus.main_title");
		put("enderchest_gui_title", "menus.chest_title");
		put("enderchest_glasspane_title", "menus.pane_title");
		put("enderchest_empty", "menus.chest_empty");
		put("enderchest_inventoryfull", "menus.chest_full");
		put("enderchest_locked", "menus.chest_locked");

		put("error_cannot_open_enderchest", "errors.noperm_open_chest");
		put("error_console_denied", "errors.noperm_console");
		put("error_player_noperm", "errors.noperm_player");
		put("error_plugin_disabled", "errors.plugin_disabled");
		put("error_plugin_disabled_world", "errors.plugin_world_disabled");

		put("error_access_denied_factions", "dependencies.access_denied_factions");

		put("enderchest_nametag", "miscellaneous.chest_nametag");
	}};

	public Migration2_0() {
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
	 * Updates locales of the plugin.
	 * @return True if locales have been updated
	 */
	boolean updateLocales() {
		File localeFolder = new File(this.getDataFolder(), "locales/");
		File[] localeFiles = localeFolder.listFiles();
		if (localeFiles == null) return false;

		YamlConfiguration oldConf;
		YamlNewConfiguration newConf;
		InputStreamReader localeStreamReader;

		for (File localeFile : localeFiles) {
			oldConf = YamlConfiguration.loadConfiguration(localeFile);
			localeStreamReader = new InputStreamReader(EnderContainers.getInstance().getResource("locale.yml"), Charsets.UTF_8);
			newConf = YamlNewConfiguration.loadConfiguration(localeStreamReader);

			// Apply old configuration on new configuration
			newConf.applyConfiguration(oldConf, LOCALE_LINK_MAP);

			try {
				newConf.save(localeFile);
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			} finally {
				// Close the locale stream reader in any case
				try {
					localeStreamReader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		return true;
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

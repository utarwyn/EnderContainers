package fr.utarwyn.endercontainers.migration.migration2_0;

import fr.utarwyn.endercontainers.util.EUtil;
import fr.utarwyn.endercontainers.util.ItemSerializer;
import fr.utarwyn.endercontainers.util.Log;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

/**
 * Represents the migration for 2.X versions but for a flatfile configuration.
 * @since 2.0.0
 * @author Utarwyn
 */
public class MigrationFlat2_0 extends Migration2_0 {

	@Override
	public void perform() {
		/* ------------------- */
		/*  Format backups...  */
		/* ------------------- */
		Log.log("Reconfigure backups...", true);
		if (!this.reconfigureBackups()) return;

		/* ---------------------- */
		/*  Copy chests files...  */
		/* ---------------------- */
		Log.log("Move enderchests...", true);
		if (!this.moveEnderchests()) return;

		/* -------------------------------- */
		/*  Format chests configuration...  */
		/* -------------------------------- */
		Log.log("Reconfigure enderchests...", true);
		if (!this.reconfigureEnderchests()) return;

		/* ------------------------- */
		/*  Format configuration...  */
		/* ------------------------- */
		Log.log("Apply old configuration...", true);
		if (!this.updateConfiguration()) return;

		/* ------------------- */
		/*  Update locales...  */
		/* ------------------- */
		Log.log("Update locales...", true);
		this.updateLocales();
	}

	/**
	 * Moves all enderchests into the new chests folder
	 * @return True if all enderchests have been moved
	 */
	private boolean moveEnderchests() {
		File fromChestsFolder = new File(this.getDataFolder(), "enderchests/");
		File destChestsFolder = new File(this.getDataFolder(), "data/");

		return (destChestsFolder.exists() || destChestsFolder.mkdirs())     &&
				this.copyAndRenameFiles(fromChestsFolder, destChestsFolder) &&
				fromChestsFolder.delete();
	}

	@Override
	boolean reconfigureBackups() {
		// Reconfigure backups.yml file
		File file = new File(this.getDataFolder(), "backups.yml");
		if (!file.exists()) return true;

		YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
		File backupsDir = new File(this.getDataFolder(), "backups/");

		if (config.isConfigurationSection("backups"))
			for (String key : config.getConfigurationSection("backups").getKeys(false)) {
				ConfigurationSection section = config.getConfigurationSection("backups." + key);
				String strDate = section.getString("date");

				section.set("path", null);
				section.set("date", Long.valueOf(strDate));

				// Move the backup folder
				File oldFolder = new File(this.getDataFolder(), "enderchests/backup_" + strDate + "/");
				if (oldFolder.exists()) {
					File newFolder = new File(backupsDir, section.getString("name") + "/");

					if (!newFolder.exists() && !newFolder.mkdirs())
						return false;
					if (!this.copyAndRenameFiles(oldFolder, newFolder) || !EUtil.deleteFolder(oldFolder))
						return false;
				}
			}

		try {
			config.save(file);
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}

		return true;
	}

	@Override
	boolean reconfigureEnderchests() {
		YamlConfiguration conf, playerConf;
		Map<Integer, Integer> chestAccesses;
		File playerFile;

		Map<Integer, ItemStack> chestItems;
		long lastSaved;

		List<File> chestFiles = this.getChestFiles();

		playerFile = new File(this.getDataFolder(), "players.yml");
		playerConf = YamlConfiguration.loadConfiguration(playerFile);

		if (chestFiles != null) {
			for (File chestFile : chestFiles) {
				// Load the configuration file ...
				conf = YamlConfiguration.loadConfiguration(chestFile);
				chestAccesses = parseAccessesFor(playerConf, conf.getString("playername"));

				// Remove unused configuration
				conf.set("playername", null);
				conf.set("enderchestsSize", null);

				// Gets old configuration
				lastSaved = Long.valueOf(conf.getString("lastsaved")) / 1000; // UNIX format
				conf.set("lastsaved", null);

				// Create new enderchests list
				for (String key : conf.getConfigurationSection("enderchests").getKeys(false)) {
					int num = Integer.parseInt(key.replace("enderchest", ""));
					int rows = chestAccesses.getOrDefault(num, 3);

					chestItems = new HashMap<>();

					for (String itemKey : conf.getConfigurationSection("enderchests." + key).getKeys(false))
						chestItems.put(Integer.parseInt(itemKey), conf.getItemStack("enderchests." + key + "." + itemKey));

					conf.set("enderchests." + num + ".rows", rows);
					conf.set("enderchests." + num + ".position", num);
					conf.set("enderchests." + num + ".contents", ItemSerializer.itemsToString(chestItems));
					conf.set("enderchests." + num + ".lastLocking", lastSaved);

					conf.set("enderchests.enderchest" + num, null);
				}

				// ... and save the configuration file!
				try {
					conf.save(chestFile);
				} catch (IOException e) {
					e.printStackTrace();
					return false;
				}
			}
		}

		return playerFile.delete();
	}

	/**
	 * Returns all files in which there are chests configurations
	 * @return Enderchests files
	 */
	private List<File> getChestFiles() {
		List<File> files = new ArrayList<>();

		// Get all normal chest files
		files.addAll(Arrays.asList(Objects.requireNonNull(new File(this.getDataFolder(), "data/").listFiles())));

		// Add all backups chests
		for (File backupFolder : Objects.requireNonNull(new File(this.getDataFolder(), "backups/").listFiles()))
			if (backupFolder.isDirectory())
				files.addAll(Arrays.asList(Objects.requireNonNull(backupFolder.listFiles())));

		return files;
	}

	/**
	 * Copy and rename chest files from a folder to another folder.
	 * This method remove hyphens in filenames to supports 2.X versions
	 *
	 * @param fromFolder Source folder
	 * @param destFolder Destination folder
	 * @return True if files have been copied and renamed
	 */
	private boolean copyAndRenameFiles(File fromFolder, File destFolder) {
		File[] fromFiles = fromFolder.listFiles();

		if (fromFiles != null) {
			for (File fromFile : fromFiles) {
				try {
					Files.copy(fromFile.toPath(), new File(destFolder, fromFile.getName().replaceAll("-", "")).toPath());
					if (!fromFile.delete()) return false;
				} catch (IOException e) {
					e.printStackTrace();
					return false;
				}
			}
		}

		return true;
	}

	/**
	 * Parses accesses for a given player in a specific configuration
	 * @param conf Configuration used to get accesses of the player
	 * @param playername Name of the player to get accesses in the configuration file
	 * @return A map with all accesses of the player
	 */
	private Map<Integer, Integer> parseAccessesFor(YamlConfiguration conf, String playername) {
		Map<Integer, Integer> map = new HashMap<>();
		String accesses = conf.getString(playername + ".accesses");

		if (accesses != null) {
			String[] tuples = accesses.split(";");

			for (String tuple : tuples) {
				String[] dataAcc = tuple.split(":");
				map.put(Integer.parseInt(dataAcc[0]), Integer.parseInt(dataAcc[1]));
			}
		}

		return map;
	}

}

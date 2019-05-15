package fr.utarwyn.endercontainers;

import fr.utarwyn.endercontainers.backup.BackupManager;
import fr.utarwyn.endercontainers.command.AbstractCommand;
import fr.utarwyn.endercontainers.command.EnderchestCommand;
import fr.utarwyn.endercontainers.command.MainCommand;
import fr.utarwyn.endercontainers.configuration.Files;
import fr.utarwyn.endercontainers.database.DatabaseManager;
import fr.utarwyn.endercontainers.dependency.DependenciesManager;
import fr.utarwyn.endercontainers.enderchest.EnderChestManager;
import fr.utarwyn.endercontainers.hologram.HologramManager;
import fr.utarwyn.endercontainers.migration.MigrationManager;
import fr.utarwyn.endercontainers.util.Updater;
import org.bstats.bukkit.Metrics;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

/**
 * Main class of the plugin. Everything starts here.
 * The plugin is under license. Please see LICENSE file to have more info.
 *
 * @since 1.0.0
 * @author Utarwyn
 */
public class EnderContainers extends JavaPlugin {

	/**
	 * Download link of the plugin
	 */
	public static final String DOWNLOAD_LINK = "http://bit.ly/2A8Xv8S";

	/**
	 * The plugin prefix
	 */
	public static final String PREFIX = "§8[§6EnderContainers§8] §7";

	/**
	 * Prefix for all permissions of the plugin
	 */
	public static final String PERM_PREFIX = "endercontainers.";

	/**
	 * The Endercontainers instance
	 */
	private static EnderContainers instance;

	/**
	 * Called when the plugin loads
	 */
	@Override
	public void onEnable() {
		instance = this;

		// Load the plugin's configuration
		if (!Files.initConfiguration(this)) {
			this.getLogger().log(Level.SEVERE, "Cannot load the plugin's configuration. Please check the above log. Plugin loading failed.");
			return;
		}

		// Now we have to load needed managers for the migration system
		new DependenciesManager();
		new DatabaseManager();

		// Load the migration manager and stop the plugin if a migration have been done.
		MigrationManager mm = new MigrationManager();
		if (mm.hasDoneMigration()) {
			this.getServer().getPluginManager().disablePlugin(this);
			return;
		}

		// Load others managers ...
		new EnderChestManager();
		new BackupManager();
		new HologramManager();

		// Load plugin locale ...
		if (!Files.initLocale(this)) {
			this.getLogger().log(Level.SEVERE, "Cannot load the plugin's locale. Please check the above log. Plugin loading failed.");
			return;
		}

		// Load commands ...
		AbstractCommand.register(new MainCommand());
		AbstractCommand.register(new EnderchestCommand());

		// Check for update if needed ...
		if (Files.getConfiguration().isUpdateChecker()) {
			Updater.getInstance().notifyUpToDate();
		}

		// Load metrics (bStats) ...
		new Metrics(this);
	}

	/**
	 * Called when the plugin disables
	 */
	@Override
	public void onDisable() {
		// Unload all managers
		Managers.unloadAll();
	}

	/**
	 * Allows to get the main instance of the plugin from all classes of the plugin.
	 * @return The EnderContainers plugin instance.
	 */
	public static EnderContainers getInstance() {
		return EnderContainers.instance;
	}

	/**
	 * Get the instance of a registered manager by its class.
	 * This method causes a warning if the manager cannot be found because
	 * it's not a normal phenomenon.
	 *
	 * @param clazz Class searched into the manager collection
	 * @param <T> Generic type which represents the manager object
	 * @return Registered manager if found otherwise null
	 */
	public final <T> T getInstance(Class<T> clazz) {
		T inst = Managers.getInstance(clazz);

		if (inst == null)
			this.getLogger().log(Level.WARNING, clazz + " instance is null!");

		return inst;
	}

}

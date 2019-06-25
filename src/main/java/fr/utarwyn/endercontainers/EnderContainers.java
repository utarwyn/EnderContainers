package fr.utarwyn.endercontainers;

import fr.utarwyn.endercontainers.backup.BackupManager;
import fr.utarwyn.endercontainers.command.CommandManager;
import fr.utarwyn.endercontainers.configuration.Files;
import fr.utarwyn.endercontainers.database.DatabaseManager;
import fr.utarwyn.endercontainers.dependency.DependenciesManager;
import fr.utarwyn.endercontainers.enderchest.EnderChestManager;
import fr.utarwyn.endercontainers.hologram.HologramManager;
import fr.utarwyn.endercontainers.migration.MigrationManager;
import fr.utarwyn.endercontainers.util.Updater;
import org.bstats.bukkit.Metrics;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;
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

        // Now we have to load core managers of the plugin
        Managers.registerManager(this, CommandManager.class);
		Managers.registerManager(this, DependenciesManager.class);
		Managers.registerManager(this, DatabaseManager.class);

		// Load the migration manager and stop the plugin if a migration have been done.
		MigrationManager mm = Managers.registerManager(this, MigrationManager.class);
		if (mm != null && mm.hasDoneMigration()) {
			this.getServer().getPluginManager().disablePlugin(this);
			return;
		}

		// Load others managers ...
		Managers.registerManager(this, EnderChestManager.class);
		Managers.registerManager(this, BackupManager.class);
		Managers.registerManager(this, HologramManager.class);

		// Load plugin locale ...
		if (!Files.initLocale(this)) {
			this.getLogger().log(Level.SEVERE, "Cannot load the plugin's locale. Please check the above log. Plugin loading failed.");
			return;
		}

		// Check for update if needed ...
		if (Files.getConfiguration().isUpdateChecker()) {
			Updater.getInstance().notifyUpToDate();
		}

        // Load commands ...
        Objects.requireNonNull(Managers.getInstance(CommandManager.class)).registerCommands();

		// Load metrics (bStats) ...
		new Metrics(this);
	}

	/**
	 * Called when the plugin disables
	 */
	@Override
	public void onDisable() {
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
	public final <T extends AbstractManager> T getManager(Class<T> clazz) {
		T inst = Managers.getInstance(clazz);

		if (inst == null) {
			this.getLogger().log(Level.WARNING, clazz + " instance is null!");
		}

		return inst;
	}

}

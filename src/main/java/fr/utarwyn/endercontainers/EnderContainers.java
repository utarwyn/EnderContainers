package fr.utarwyn.endercontainers;

import fr.utarwyn.endercontainers.backup.BackupManager;
import fr.utarwyn.endercontainers.commands.AbstractCommand;
import fr.utarwyn.endercontainers.commands.EnderchestCommand;
import fr.utarwyn.endercontainers.commands.MainCommand;
import fr.utarwyn.endercontainers.database.DatabaseManager;
import fr.utarwyn.endercontainers.dependencies.DependenciesManager;
import fr.utarwyn.endercontainers.enderchest.EnderChestManager;
import fr.utarwyn.endercontainers.hologram.HologramManager;
import fr.utarwyn.endercontainers.migration.MigrationManager;
import fr.utarwyn.endercontainers.util.Locale;
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
	 * The Endercontainers instance
	 */
	private static EnderContainers instance;

	/**
	 * Called when the plugin loads
	 */
	@Override
	public void onEnable() {
		instance = this;

		// Load main configuration ...
		if (!Config.get().initialize(this))
			return;

		// Load needed managers ...
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
		if (!Locale.get().initialize(this))
			return;

		// Load commands ...
		AbstractCommand.register(new MainCommand());
		AbstractCommand.register(new EnderchestCommand());

		// Check for update if needed ...
		if (Config.updateChecker)
			Updater.getInstance().notifyUpToDate();

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

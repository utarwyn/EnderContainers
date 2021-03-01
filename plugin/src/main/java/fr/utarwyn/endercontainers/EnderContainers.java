package fr.utarwyn.endercontainers;

import fr.utarwyn.endercontainers.backup.BackupManager;
import fr.utarwyn.endercontainers.command.CommandManager;
import fr.utarwyn.endercontainers.compatibility.nms.NMSUtil;
import fr.utarwyn.endercontainers.configuration.Files;
import fr.utarwyn.endercontainers.configuration.wrapper.YamlFileLoadException;
import fr.utarwyn.endercontainers.database.DatabaseManager;
import fr.utarwyn.endercontainers.dependency.DependenciesManager;
import fr.utarwyn.endercontainers.enderchest.EnderChestManager;
import fr.utarwyn.endercontainers.hologram.HologramManager;
import fr.utarwyn.endercontainers.menu.MenuManager;
import fr.utarwyn.endercontainers.storage.StorageManager;
import fr.utarwyn.endercontainers.util.MetricsHandler;
import fr.utarwyn.endercontainers.util.Updater;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;
import java.util.logging.Level;

/**
 * Main class of the plugin. Everything starts here.
 * The plugin is under license. Please see LICENSE file to have more info.
 *
 * @author Utarwyn
 * @since 1.0.0
 */
public class EnderContainers extends JavaPlugin {

    /**
     * The plugin prefix
     */
    public static final String PREFIX = "§8[§6EnderContainers§8] §7";

    /**
     * The Endercontainers instance
     */
    private static EnderContainers instance;

    /**
     * Allows to get the main instance of the plugin from all classes of the plugin.
     *
     * @return The EnderContainers plugin instance.
     */
    public static EnderContainers getInstance() {
        return EnderContainers.instance;
    }

    /**
     * Called when the plugin loads
     */
    @Override
    public void onEnable() {
        instance = this;

        // Load config files
        try {
            Files.initConfiguration(this);
            Files.initLocale(this);
        } catch (YamlFileLoadException e) {
            this.getLogger().log(Level.SEVERE,
                    "Cannot load plugin configuration or messages file", e);
            this.getPluginLoader().disablePlugin(this);
            return;
        }

        // Load all managers
        Managers.register(this, CommandManager.class);
        Managers.register(this, MenuManager.class);
        Managers.register(this, DependenciesManager.class);
        Managers.register(this, DatabaseManager.class);
        Managers.register(this, StorageManager.class);
        Managers.register(this, EnderChestManager.class);
        Managers.register(this, BackupManager.class);
        Managers.register(this, HologramManager.class);
        Managers.register(this, Updater.class);

        // Registering commands
        Objects.requireNonNull(Managers.get(CommandManager.class)).registerCommands();

        // Initialize the metrics handler
        new MetricsHandler(this);
    }

    /**
     * Called when the plugin disables
     */
    @Override
    public void onDisable() {
        Managers.unloadAll();
        Managers.clear();
    }

    /**
     * Executes a task in the primary thread of the server.
     *
     * @param runnable task to execute in the main thread
     */
    public void executeTaskOnMainThread(Runnable runnable) {
        if (getServer().isPrimaryThread()) {
            runnable.run();
        } else {
            getServer().getScheduler().scheduleSyncDelayedTask(this, runnable);
        }
    }

    /**
     * Executes a task in another thread of the server.
     *
     * @param runnable task to execute in another thread
     */
    public void executeTaskOnOtherThread(Runnable runnable) {
        if (NMSUtil.isAsyncDisabled()) {
            runnable.run();
        } else {
            getServer().getScheduler().runTaskAsynchronously(this, runnable);
        }
    }

}

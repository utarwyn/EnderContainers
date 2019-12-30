package fr.utarwyn.endercontainers.backup.action;

import fr.utarwyn.endercontainers.EnderContainers;
import fr.utarwyn.endercontainers.backup.BackupManager;

/**
 * Represents an action which can be performed on a backup.
 *
 * @author Utarwyn
 * @since 2.3.0
 */
public abstract class BackupAbstractTask implements Runnable {

    /**
     * The backup manager
     */
    protected BackupManager manager;

    /**
     * Object to call when the action is finished
     */
    protected ActionCallback callback;

    /**
     * The EnderContainers plugin
     */
    private EnderContainers plugin;

    /**
     * Construct a new backup abstract task
     *
     * @param plugin   the EnderContainers plugin
     * @param manager  the backup manager
     * @param callback callable object at the end of the task
     */
    public BackupAbstractTask(EnderContainers plugin, BackupManager manager, ActionCallback callback) {
        this.plugin = plugin;
        this.manager = manager;
        this.callback = callback;
    }

    /**
     * Run a callable object at the end of the task.
     *
     * @param result action result
     */
    protected void runCallback(boolean result) {
        this.plugin.getServer().getScheduler().scheduleSyncDelayedTask(this.plugin, () -> this.callback.run(result));
    }

}

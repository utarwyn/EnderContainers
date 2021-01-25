package fr.utarwyn.endercontainers.backup.action;

import fr.utarwyn.endercontainers.EnderContainers;
import fr.utarwyn.endercontainers.backup.BackupManager;

import java.util.function.Consumer;

/**
 * Represents an action which can be performed on a backup.
 *
 * @author Utarwyn
 * @since 2.2.0
 */
public abstract class BackupAbstractTask implements Runnable {

    /**
     * The EnderContainers plugin
     */
    private final EnderContainers plugin;

    /**
     * The backup manager
     */
    protected BackupManager manager;

    /**
     * Object to consume when the action is finished
     */
    protected Consumer<Boolean> callback;

    /**
     * Construct a new backup abstract task
     *
     * @param plugin   the EnderContainers plugin
     * @param manager  the backup manager
     * @param consumer object consumed at the end of the task
     */
    protected BackupAbstractTask(EnderContainers plugin, BackupManager manager,
                                 Consumer<Boolean> consumer) {
        this.plugin = plugin;
        this.manager = manager;
        this.callback = consumer;
    }

    /**
     * Supply the result of the task to the registered consumer.
     *
     * @param result action result
     */
    protected void supplyResult(boolean result) {
        this.plugin.getServer().getScheduler().scheduleSyncDelayedTask(this.plugin,
                () -> this.callback.accept(result));
    }

}

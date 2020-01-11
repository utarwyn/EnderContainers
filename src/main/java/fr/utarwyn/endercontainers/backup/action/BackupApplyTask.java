package fr.utarwyn.endercontainers.backup.action;

import fr.utarwyn.endercontainers.EnderContainers;
import fr.utarwyn.endercontainers.Managers;
import fr.utarwyn.endercontainers.backup.Backup;
import fr.utarwyn.endercontainers.backup.BackupManager;
import fr.utarwyn.endercontainers.enderchest.EnderChestManager;

/**
 * Task to asynchronously apply a backup by replacing all current data.
 *
 * @author Utarwyn
 * @since 2.3.0
 */
public class BackupApplyTask extends BackupAbstractTask {

    /**
     * Backup to apply
     */
    private Backup backup;

    /**
     * Construct a new task to apply a backup.
     *
     * @param plugin   the EnderContainers plugin
     * @param manager  the backup manager
     * @param backup   backup to apply
     * @param callback object to call at the end of the task
     */
    public BackupApplyTask(EnderContainers plugin, BackupManager manager, Backup backup, ActionCallback callback) {
        super(plugin, manager, callback);
        this.backup = backup;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void run() {
        boolean result = false;

        if (this.manager.getStorage().applyBackup(this.backup)) {
            result = Managers.reload(EnderChestManager.class);
        }

        this.runCallback(result);
    }

}

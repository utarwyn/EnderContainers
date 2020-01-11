package fr.utarwyn.endercontainers.backup.action;

import fr.utarwyn.endercontainers.EnderContainers;
import fr.utarwyn.endercontainers.backup.Backup;
import fr.utarwyn.endercontainers.backup.BackupManager;

/**
 * Task to asynchronously remove a backup and all of its data.
 *
 * @author Utarwyn
 * @since 2.3.0
 */
public class BackupRemoveTask extends BackupAbstractTask {

    /**
     * Backup to remove
     */
    private Backup backup;

    /**
     * Construct a new task to remove a backup.
     *
     * @param plugin   the EnderContainers plugin
     * @param manager  the backup manager
     * @param backup   backup to remove
     * @param callback object to call at the end of the task
     */
    public BackupRemoveTask(EnderContainers plugin, BackupManager manager, Backup backup, ActionCallback callback) {
        super(plugin, manager, callback);
        this.backup = backup;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void run() {
        this.runCallback(this.manager.getStorage().removeBackup(backup)
                && this.manager.getBackups().remove(backup));
    }

}

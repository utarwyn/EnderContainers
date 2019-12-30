package fr.utarwyn.endercontainers.backup.action;

import fr.utarwyn.endercontainers.EnderContainers;
import fr.utarwyn.endercontainers.backup.Backup;
import fr.utarwyn.endercontainers.backup.BackupManager;

import java.sql.Timestamp;

/**
 * Task to asynchronously create a backup with a name and an operator.
 *
 * @author Utarwyn
 * @since 2.3.0
 */
public class BackupCreateTask extends BackupAbstractTask {

    /**
     * Operator of the creation
     */
    private String operator;

    /**
     * Name of the backup to create
     */
    private String name;

    /**
     * Construct a new task to create a backup.
     *
     * @param plugin   the EnderContainers plugin
     * @param manager  the backup manager
     * @param operator operator which creates the backup
     * @param name     name of the backup to create
     * @param callback object to call at the end of the task
     */
    public BackupCreateTask(EnderContainers plugin, BackupManager manager, String operator,
                            String name, ActionCallback callback) {
        super(plugin, manager, callback);
        this.operator = operator;
        this.name = name;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void run() {
        boolean result = false;

        if (!this.manager.getBackupByName(name).isPresent()) {
            // Create a new backup
            Backup backup = new Backup(
                    this.name, new Timestamp(System.currentTimeMillis()),
                    "all", this.operator
            );

            // Save the backup in the proper config and execute the backup
            if (this.manager.getStorage().saveNewBackup(backup) && this.manager.getStorage().executeStorage(backup)) {
                result = this.manager.getBackups().add(backup);
            }
        }

        this.runCallback(result);
    }

}

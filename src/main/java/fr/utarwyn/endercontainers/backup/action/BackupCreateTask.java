package fr.utarwyn.endercontainers.backup.action;

import fr.utarwyn.endercontainers.EnderContainers;
import fr.utarwyn.endercontainers.backup.Backup;
import fr.utarwyn.endercontainers.backup.BackupManager;

import java.sql.Timestamp;
import java.util.function.Consumer;

/**
 * Task to asynchronously create a backup with a name and an operator.
 *
 * @author Utarwyn
 * @since 2.2.0
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
     * @param consumer object to consum at the end of the task
     */
    public BackupCreateTask(EnderContainers plugin, BackupManager manager, String operator,
                            String name, Consumer<Boolean> consumer) {
        super(plugin, manager, consumer);
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

        this.supplyResult(result);
    }

}

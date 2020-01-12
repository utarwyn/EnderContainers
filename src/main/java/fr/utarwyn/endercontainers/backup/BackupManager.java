package fr.utarwyn.endercontainers.backup;

import fr.utarwyn.endercontainers.AbstractManager;
import fr.utarwyn.endercontainers.Managers;
import fr.utarwyn.endercontainers.backup.action.ActionCallback;
import fr.utarwyn.endercontainers.backup.action.BackupApplyTask;
import fr.utarwyn.endercontainers.backup.action.BackupCreateTask;
import fr.utarwyn.endercontainers.backup.action.BackupRemoveTask;
import fr.utarwyn.endercontainers.storage.StorageManager;
import fr.utarwyn.endercontainers.storage.backups.BackupsData;

import java.util.List;
import java.util.Optional;

/**
 * Allows us to manage backups inside the plugin
 *
 * @author Utarwyn
 * @since 2.0.0
 */
public class BackupManager extends AbstractManager {

    /**
     * The list of all backups stored in memory
     */
    private List<Backup> backups;

    /**
     * Class which manage the external storage of backups
     */
    private BackupsData storage;

    /**
     * {@inheritDoc}
     */
    @Override
    public void load() {
        this.storage = Managers.get(StorageManager.class).createBackupDataStorage();
        this.backups = this.storage.getCachedBackups();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void unload() {
        this.storage = null;
    }

    /**
     * Get the internal backups storage
     *
     * @return backups storage
     */
    public BackupsData getStorage() {
        return this.storage;
    }

    /**
     * Returns the list of backups stored in memory
     *
     * @return The list of backups
     */
    public List<Backup> getBackups() {
        return this.backups;
    }

    /**
     * Returns a backup by its name
     *
     * @param name name used to do the research
     * @return backup if it was found by its name
     */
    public Optional<Backup> getBackupByName(String name) {
        return this.backups.stream()
                .filter(backup -> backup.getName().equals(name))
                .findAny();
    }

    /**
     * Asynchronously create a backup with a name and an operator.
     *
     * @param name     name of the backup to create
     * @param operator name of the player who triggered this action
     * @param callback method ran when the action is finished
     */
    public void createBackup(String name, String operator, ActionCallback callback) {
        this.plugin.getServer().getScheduler().runTaskAsynchronously(
                this.plugin,
                new BackupCreateTask(this.plugin, this, operator, name, callback)
        );
    }

    /**
     * Asynchronously apply a backup by replacing all current data.
     *
     * @param name     name of the backup to apply
     * @param callback method ran when the action is finished
     */
    public void applyBackup(String name, ActionCallback callback) {
        Optional<Backup> backup = this.getBackupByName(name);

        if (backup.isPresent()) {
            this.plugin.getServer().getScheduler().runTaskAsynchronously(
                    this.plugin,
                    new BackupApplyTask(this.plugin, this, backup.get(), callback)
            );
        } else {
            callback.run(false);
        }
    }

    /**
     * Asynchronously remove a backup and all of its data.
     *
     * @param name     name of the backup to remove
     * @param callback method ran when the action is finished
     */
    public void removeBackup(String name, ActionCallback callback) {
        Optional<Backup> backup = this.getBackupByName(name);

        if (backup.isPresent()) {
            this.plugin.getServer().getScheduler().runTaskAsynchronously(
                    this.plugin,
                    new BackupRemoveTask(this.plugin, this, backup.get(), callback)
            );
        } else {
            callback.run(false);
        }
    }

}

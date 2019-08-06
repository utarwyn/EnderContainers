package fr.utarwyn.endercontainers.backup;

import fr.utarwyn.endercontainers.AbstractManager;
import fr.utarwyn.endercontainers.Managers;
import fr.utarwyn.endercontainers.enderchest.EnderChestManager;
import fr.utarwyn.endercontainers.storage.StorageWrapper;
import fr.utarwyn.endercontainers.storage.backups.BackupsData;

import java.sql.Timestamp;
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
    private BackupsData backupsStorage;

    /**
     * {@inheritDoc}
     */
    @Override
    public void load() {
        this.backupsStorage = StorageWrapper.get(BackupsData.class);
        assert this.backupsStorage != null;

        this.backups = this.backupsStorage.getCachedBackups();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void unload() {
        StorageWrapper.unload(BackupsData.class);
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
     * Create a backup with a name and an author
     *
     * @param name    The name of the backup which have to be created.
     * @param creator The name of the player who load this action.
     * @return True if the backup was created with success.
     */
    public boolean createBackup(String name, String creator) {
        if (this.getBackupByName(name).isPresent()) {
            return false;
        }

        // Create a new backup
        Backup backup = new Backup(
                name, new Timestamp(System.currentTimeMillis()),
                "all", creator
        );

        // Save the backup in the proper config
        if (!this.backupsStorage.saveNewBackup(backup)) {
            return false;
        }

        // Execute the backup (save all enderchests)
        if (!this.backupsStorage.executeStorage(backup)) {
            return false;
        }

        return this.backups.add(backup);
    }

    /**
     * Apply a backup indefinitly and erase all older enderchests
     *
     * @param name name of the backup which have to be applied
     * @return true if the backup has been properly applied
     */
    public boolean applyBackup(String name) {
        Optional<Backup> backup = this.getBackupByName(name);

        if (backup.isPresent() && this.backupsStorage.applyBackup(backup.get())) {
            Managers.reload(EnderChestManager.class);
            return true;
        }

        return false;
    }

    /**
     * Remove a backup from data & memory by its name.
     *
     * @param name name of the backup which have to be removed
     * @return true if the backup has been properly removed
     */
    public boolean removeBackup(String name) {
        Optional<Backup> backup = this.getBackupByName(name);
        return backup.isPresent()
                && this.backupsStorage.removeBackup(backup.get())
                && this.backups.remove(backup.get());
    }

}

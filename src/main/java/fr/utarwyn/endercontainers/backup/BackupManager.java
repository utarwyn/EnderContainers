package fr.utarwyn.endercontainers.backup;

import fr.utarwyn.endercontainers.AbstractManager;
import fr.utarwyn.endercontainers.EnderContainers;
import fr.utarwyn.endercontainers.Managers;
import fr.utarwyn.endercontainers.enderchest.EnderChestManager;
import fr.utarwyn.endercontainers.storage.StorageWrapper;
import fr.utarwyn.endercontainers.storage.backups.BackupsData;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * Allows us to manage backups inside the plugin
 * @since 2.0.0
 * @author Utarwyn
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
	 * Construct the manager
	 */
	public BackupManager() {
		super(EnderContainers.getInstance());
	}

	/**
	 * Called when the manager is initializing
	 */
	@Override
	public void initialize() {
		this.backupsStorage = StorageWrapper.get(BackupsData.class);
		assert this.backupsStorage != null;

		this.backups = this.backupsStorage.getCachedBackups();
	}

	/**
	 * Called when the manager is unloading
	 */
	@Override
	protected void unload() {
		StorageWrapper.unload(BackupsData.class);
	}

	/**
	 * Returns the list of backups stored in memory
	 * @return The list of backups
	 */
	public List<Backup> getBackups() {
		return new ArrayList<>(this.backups);
	}

	/**
	 * Returns a backup by its name
	 * @param name Name used to do the research
	 * @return The backup found by its name or null if not found.
	 */
	public Backup getBackupByName(String name) {
		for (Backup backup : this.backups)
			if (backup.getName().equals(name))
				return backup;

		return null;
	}

	/**
	 * Create a backup with a name and an author
	 * @param name The name of the backup which have to be created.
	 * @param creator The name of the player who initialize this action.
	 * @return True if the backup was created with success.
	 */
	public boolean createBackup(String name, String creator) {
		if (this.getBackupByName(name) != null) return false;
		// Create a new backup
		Backup backup = new Backup(name, new Timestamp(System.currentTimeMillis()), "all", creator);

		// Save the backup in the proper config
		if (!this.backupsStorage.saveNewBackup(backup))
			return false;
		// Execute the backup (save all enderchests)
		if (!this.backupsStorage.executeStorage(backup))
			return false;

		// Register the backup in memory
		this.backups.add(backup);
		return true;
	}

	/**
	 * Apply a backup indefinitly and erase all older enderchests
	 * @param name The name of the backup which have to be applied
	 * @return True if the backup has been properly applied.
	 */
	public boolean applyBackup(String name) {
		Backup backup = this.getBackupByName(name);
		if (backup == null || !this.backupsStorage.applyBackup(backup))
			return false;

		Managers.reload(EnderChestManager.class);
		return true;
	}

	/**
	 * Remove a backup from data & memory by its name.
	 * @param name The name of the backup which have to be removed.
	 * @return True if the backup has been properly removed.
	 */
	public boolean removeBackup(String name) {
		Backup backup = this.getBackupByName(name);
		return backup != null && this.backupsStorage.removeBackup(backup) && this.backups.remove(backup);
	}

}

package fr.utarwyn.endercontainers.storage.backups;

import fr.utarwyn.endercontainers.backup.Backup;
import fr.utarwyn.endercontainers.storage.StorageWrapper;

import java.util.List;

/**
 * Storage wrapper to manage backups data.
 * @since 2.0.0
 * @author Utarwyn
 */
public abstract class BackupsData extends StorageWrapper {

	/**
	 * List of cached backups retrieved
	 */
	List<Backup> backups;

	@Override
	protected boolean equals(Object... params) {
		return true;
	}

	protected boolean isUnused() {
		return false;
	}

	/**
	 * Get the list of all cached backups
	 * @return List of backups
	 */
	public List<Backup> getCachedBackups() {
		return this.backups;
	}

	/**
	 * Save a new backup
	 * @param backup Backup object to save
	 * @return True if the backup was sucessfully saved
	 */
	public abstract boolean saveNewBackup(Backup backup);

	/**
	 * Execute the backup. It means that all data will be saved in
	 * another place to create a "backup".
	 * @param backup Backup object to execute
	 * @return True if the backup was sucessfully executed
	 */
	public abstract boolean executeStorage(Backup backup);

	/**
	 * Apply a backup. It means that all data of the plugin will be
	 * replaced by the data of the backup.
	 * @param backup Backup object to apply
	 * @return True if the backup was sucessfully applied
	 */
	public abstract boolean applyBackup(Backup backup);

	/**
	 * Remove a backup
	 * @param backup Backup object to remove
	 * @return True if the backup was sucessfully removed
	 */
	public abstract boolean removeBackup(Backup backup);

}

package fr.utarwyn.endercontainers.storage;

import fr.utarwyn.endercontainers.AbstractManager;
import fr.utarwyn.endercontainers.Managers;
import fr.utarwyn.endercontainers.database.DatabaseManager;
import fr.utarwyn.endercontainers.storage.backups.BackupsData;
import fr.utarwyn.endercontainers.storage.backups.BackupsFlatData;
import fr.utarwyn.endercontainers.storage.backups.BackupsSQLData;
import fr.utarwyn.endercontainers.storage.player.PlayerData;
import fr.utarwyn.endercontainers.storage.player.PlayerFlatData;
import fr.utarwyn.endercontainers.storage.player.PlayerSQLData;

import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Manage different types of storages (SQL or in flat file).
 * Choose right classes to use at each plugin load.
 *
 * @author Utarwyn
 * @since 2.2.0
 */
public class StorageManager extends AbstractManager {

    /**
     * This field stores the type of backup data storage to use.
     */
    private Class<? extends BackupsData> backupDataPattern;

    /**
     * This field stores the type of player data storage to use.
     */
    private Class<? extends PlayerData> playerDataPattern;

    /**
     * {@inheritDoc}
     */
    @Override
    public void load() {
        // Use the good type of storage by checking the database state
        DatabaseManager databaseManager = Managers.get(DatabaseManager.class);
        boolean useSqlStorage = databaseManager.isReady();

        this.backupDataPattern = useSqlStorage ? BackupsSQLData.class : BackupsFlatData.class;
        this.playerDataPattern = useSqlStorage ? PlayerSQLData.class : PlayerFlatData.class;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void unload() {
        this.backupDataPattern = null;
        this.playerDataPattern = null;
    }

    /**
     * Create a new object to store backup data.
     *
     * @return backup data storage object
     */
    public BackupsData createBackupDataStorage() {
        try {
            return this.backupDataPattern.getDeclaredConstructor(Logger.class).newInstance(this.logger);
        } catch (ReflectiveOperationException e) {
            this.logger.log(Level.SEVERE, "Cannot instantiate a backup data storage object", e);
            return null;
        }
    }

    /**
     * Create a new object to store player data.
     *
     * @param uuid uuid of the player to manage
     * @return player data storage object
     */
    public PlayerData createPlayerDataStorage(UUID uuid) {
        try {
            return this.playerDataPattern.getDeclaredConstructor(Logger.class, UUID.class)
                    .newInstance(this.logger, uuid);
        } catch (ReflectiveOperationException e) {
            this.logger.log(Level.SEVERE, "Cannot instantiate a player data storage object", e);
            return null;
        }
    }

}

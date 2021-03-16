package fr.utarwyn.endercontainers.storage.backups;

import fr.utarwyn.endercontainers.EnderContainers;
import fr.utarwyn.endercontainers.Managers;
import fr.utarwyn.endercontainers.backup.Backup;
import fr.utarwyn.endercontainers.database.DatabaseManager;
import fr.utarwyn.endercontainers.database.DatabaseSet;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Optional;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Storage wrapper for backups (MySQL)
 *
 * @author Utarwyn
 * @since 2.0.0
 */
public class BackupsSQLData extends BackupsData {

    /**
     * The database manager
     */
    private final DatabaseManager databaseManager;

    /**
     * Construct a new backup storage wrapper with a SQL database.
     *
     * @param plugin plugin instance object
     */
    public BackupsSQLData(EnderContainers plugin) {
        super(plugin);

        this.databaseManager = Managers.get(DatabaseManager.class);
        this.backups = new ArrayList<>();

        this.load();
    }

    /**
     * Formats an enderchest stored in the database into a string
     *
     * @param set enderchest as a database row
     * @return string representation of the enderchest to be backuped
     */
    private static String formatDatabaseSet(DatabaseSet set) {
        Integer id = set.getInteger("id");
        Integer num = set.getInteger("num");
        String owner = Base64Coder.encodeString(Optional.ofNullable(set.getString("owner")).orElse(""));
        String contents = Base64Coder.encodeString(Optional.ofNullable(set.getString("contents")).orElse(""));
        Integer rows = set.getInteger("rows");

        return String.format("%d:%d:%s:%s:%d", id, num, owner, contents, rows);
    }

    /**
     * Formats an enderchest stored as a string into a database row object
     *
     * @param str enderchest as a string
     * @return database row object with chest information
     */
    private static DatabaseSet formatString(String str) {
        String[] info = str.split(":");

        int id = Integer.parseInt(info[0]);
        int num = Integer.parseInt(info[1]);
        String owner = !info[2].isEmpty() ? Base64Coder.decodeString(info[2]) : null;
        String contents = !info[3].isEmpty() ? Base64Coder.decodeString(info[3]) : null;
        int rows = Integer.parseInt(info[4]);

        DatabaseSet set = new DatabaseSet();
        set.setObject("id", id);
        set.setObject("num", num);
        set.setObject("owner", owner);
        set.setObject("contents", contents);
        set.setObject("rows", rows);
        return set;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void save() {
        // There is no file to save when using SQL
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void load() {
        try {
            this.databaseManager.getBackups().forEach(set -> this.backups.add(
                    new Backup(set.getString("name"), set.getTimestamp("date"),
                            set.getString("created_by"))
            ));
        } catch (SQLException e) {
            this.plugin.getLogger().log(Level.SEVERE,
                    "Cannot retrieve backups list from the database", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean executeStorage(Backup backup) {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean saveNewBackup(Backup backup) {
        try {
            String data = this.databaseManager.getAllEnderchests().stream()
                    .map(BackupsSQLData::formatDatabaseSet)
                    .collect(Collectors.joining(";"));

            this.databaseManager.saveBackup(
                    backup.getName(), backup.getDate().getTime(), data, backup.getCreatedBy()
            );

            return true;
        } catch (SQLException e) {
            this.plugin.getLogger().log(Level.SEVERE, String.format(
                    "Cannot save the backup %s", backup.getName()), e);
            return false;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean applyBackup(Backup backup) {
        Optional<DatabaseSet> backupSet = Optional.empty();

        try {
            backupSet = this.databaseManager.getBackup(backup.getName());
        } catch (SQLException e) {
            this.plugin.getLogger().log(Level.SEVERE, String.format(
                    "Cannot retrieve the backup %s from the database", backup.getName()
            ), e);
        }

        if (backupSet.isPresent()) {
            try {
                String backupData = backupSet.get().getString("data");

                this.databaseManager.replaceEnderchests(Stream.of(backupData.split(";"))
                        .map(BackupsSQLData::formatString)
                        .collect(Collectors.toList()));

                return true;
            } catch (SQLException e) {
                this.plugin.getLogger().log(Level.SEVERE,
                        "Cannot replace enderchests in the database", e);
            }
        }

        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean removeBackup(Backup backup) {
        try {
            return this.databaseManager.removeBackup(backup.getName());
        } catch (SQLException e) {
            this.plugin.getLogger().log(Level.SEVERE, String.format(
                    "Cannot delete backup %s from the database", backup.getName()
            ), e);
            return false;
        }
    }

}

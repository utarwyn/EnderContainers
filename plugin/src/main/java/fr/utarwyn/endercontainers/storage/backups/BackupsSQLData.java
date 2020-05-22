package fr.utarwyn.endercontainers.storage.backups;

import fr.utarwyn.endercontainers.EnderContainers;
import fr.utarwyn.endercontainers.Managers;
import fr.utarwyn.endercontainers.backup.Backup;
import fr.utarwyn.endercontainers.database.DatabaseManager;
import fr.utarwyn.endercontainers.database.DatabaseSet;
import org.apache.commons.lang.StringUtils;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.stream.Collectors;

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
     * Create a list of enderchests from a string.
     *
     * @param data string to use to generate enderchest datasets
     * @return list of generated datasets
     */
    private static List<DatabaseSet> getEnderchestsFromString(String data) {
        List<DatabaseSet> sets = new ArrayList<>();
        DatabaseSet set;

        for (String backupData : data.split(";")) {
            String[] info = backupData.split(":");

            int id = Integer.parseInt(info[0]);
            int num = Integer.parseInt(info[1]);
            String owner = Base64Coder.decodeString(info[2]);
            String contents = Base64Coder.decodeString(info[3]);
            int rows = Integer.parseInt(info[4]);

            set = new DatabaseSet();
            set.setObject("id", id);
            set.setObject("num", num);
            set.setObject("owner", owner);
            set.setObject("contents", contents);
            set.setObject("rows", rows);

            sets.add(set);
        }

        return sets;
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
            this.databaseManager.saveBackup(
                    backup.getName(), backup.getDate().getTime(),
                    this.getEnderchestsStringData(), backup.getCreatedBy()
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
                this.databaseManager.replaceEnderchests(getEnderchestsFromString(backupData));
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

    /**
     * Get all saved enderchests in the database as a string.
     *
     * @return all enderchests as a string
     */
    private String getEnderchestsStringData() {
        List<DatabaseSet> sets;

        try {
            sets = this.databaseManager.getAllEnderchests();
        } catch (SQLException e) {
            this.plugin.getLogger().log(Level.SEVERE,
                    "Cannot retrieve all enderchests from the database", e);
            return "";
        }

        List<String> dataElementList = sets.stream()
                .map(set -> set.getInteger("id") + ":"
                        + set.getInteger("num") + ":"
                        + Base64Coder.encodeString(set.getString("owner")) + ":"
                        + Base64Coder.encodeString(set.getString("contents")) + ":"
                        + set.getInteger("rows"))
                .collect(Collectors.toList());

        return StringUtils.join(dataElementList, ";");
    }

}

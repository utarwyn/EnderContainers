package fr.utarwyn.endercontainers.storage.backups;

import fr.utarwyn.endercontainers.backup.Backup;
import fr.utarwyn.endercontainers.database.DatabaseSet;
import org.apache.commons.lang.StringUtils;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

/**
 * Storage wrapper for backups (MySQL)
 *
 * @author Utarwyn
 * @since 2.0.0
 */
public class BackupsSQLData extends BackupsData {

    BackupsSQLData() {
        this.backups = new ArrayList<>();
        this.load();
    }

    @Override
    protected void load() {
        for (DatabaseSet set : getDatabaseManager().getBackups())
            this.backups.add(new Backup(
                    set.getString("name"), set.getTimestamp("date"),
                    set.getString("type"), set.getString("created_by")
            ));
    }

    @Override
    protected void save() {
        // There is no file to save when using SQL
    }

    @Override
    public boolean saveNewBackup(Backup backup) {
        try {
            getDatabaseManager().saveBackup(
                    backup.getName(), backup.getDate().getTime(), backup.getType(),
                    this.getEnderchestsStringData(), backup.getCreatedBy()
            );
            return true;
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Cannot save the backup " + backup.getName(), e);
            return false;
        }
    }

    @Override
    public boolean executeStorage(Backup backup) {
        return true;
    }

    @Override
    public boolean applyBackup(Backup backup) {
        DatabaseSet backupSet = getDatabaseManager().getBackup(backup.getName());
        if (backupSet == null) return false;

        try {
            getDatabaseManager().emptyChestTable();
            getDatabaseManager().saveEnderchestSets(this.getEnderchestsFromString(backupSet.getString("data")));
            return true;
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Cannot replace enderchests in the database", e);
            return false;
        }
    }

    @Override
    public boolean removeBackup(Backup backup) {
        return getDatabaseManager().removeBackup(backup.getName());
    }

    private String getEnderchestsStringData() {
        List<DatabaseSet> sets = getDatabaseManager().getAllEnderchests();
        if (sets == null) return "";

        List<String> dataElementList = new ArrayList<>();

        for (DatabaseSet set : sets) {
            dataElementList.add(
                    set.getInteger("id") + ":"
                            + set.getInteger("num") + ":"
                            + Base64Coder.encodeString(set.getString("owner")) + ":"
                            + Base64Coder.encodeString(set.getString("contents")) + ":"
                            + set.getInteger("rows")
            );
        }

        return StringUtils.join(dataElementList, ";");
    }

    private List<DatabaseSet> getEnderchestsFromString(String data) {
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

}

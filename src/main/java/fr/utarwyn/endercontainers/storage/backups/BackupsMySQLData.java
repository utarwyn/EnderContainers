package fr.utarwyn.endercontainers.storage.backups;

import fr.utarwyn.endercontainers.backup.Backup;
import fr.utarwyn.endercontainers.database.DatabaseSet;
import org.apache.commons.lang.StringUtils;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * Storage wrapper for backups (MySQL)
 * @since 2.0.0
 * @author Utarwyn
 */
public class BackupsMySQLData extends BackupsData {

	BackupsMySQLData() {
		this.backups = new ArrayList<>();
		this.load();
	}

	@Override
	protected void load() {
		for (DatabaseSet set : getMysqlManager().getBackups())
			this.backups.add(new Backup(
					set.getString("name"), set.getTimestamp("date"),
					set.getString("type"), set.getString("created_by")
			));
	}

	@Override
	protected void save() {

	}

	@Override
	public boolean saveNewBackup(Backup backup) {
		getMysqlManager().saveBackup(
				backup.getName(), backup.getDate().getTime(), backup.getType(), this.getEnderchestsStringData(), backup.getCreatedBy()
		);

		return true;
	}

	@Override
	public boolean executeStorage(Backup backup) {
		return true;
	}

	@Override
	public boolean applyBackup(Backup backup) {
		DatabaseSet backupSet = getMysqlManager().getBackup(backup.getName());
		if (backupSet == null) return false;

		getMysqlManager().emptyChestTable();
		getMysqlManager().saveEnderchestSets(this.getEnderchestsFromString(backupSet.getString("data")));
		return true;
	}

	@Override
	public boolean removeBackup(Backup backup) {
		return getMysqlManager().removeBackup(backup.getName());
	}

	private String getEnderchestsStringData() {
		List<DatabaseSet> sets = getMysqlManager().getAllEnderchests();
		if (sets == null) return "";

		List<String> dataElementList = new ArrayList<>();

		for (DatabaseSet set : sets) {
			if (set.getTimestamp("last_locking_time") == null)
				set.setObject("last_locking_time", new Timestamp(0));

			dataElementList.add(
					set.getInteger("id") + ":"
					+ set.getInteger("num") + ":"
					+ Base64Coder.encodeString(set.getString("owner")) + ":"
					+ Base64Coder.encodeString(set.getString("contents")) + ":"
					+ set.getInteger("rows") + ":"
					+ set.getTimestamp("last_locking_time").getTime()
			);
		}

		return StringUtils.join(dataElementList, ";");
	}

	private List<DatabaseSet> getEnderchestsFromString(String data) {
		List<DatabaseSet> sets = new ArrayList<>();
		DatabaseSet set;

		for (String backupData : data.split(";")) {
			String[] info = backupData.split(":");

			int id = Integer.valueOf(info[0]);
			int num = Integer.valueOf(info[1]);
			String owner = Base64Coder.decodeString(info[2]);
			String contents = Base64Coder.decodeString(info[3]);
			int rows = Integer.valueOf(info[4]);
			Timestamp lastLockingTime = new Timestamp(Long.valueOf(info[5]));

			set = new DatabaseSet();
			set.setObject("id", id);
			set.setObject("num", num);
			set.setObject("owner", owner);
			set.setObject("contents", contents);
			set.setObject("rows", rows);
			set.setObject("last_locking_time", lastLockingTime);

			sets.add(set);
		}

		return sets;
	}

}

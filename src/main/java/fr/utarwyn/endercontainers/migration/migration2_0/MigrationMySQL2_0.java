package fr.utarwyn.endercontainers.migration.migration2_0;

import fr.utarwyn.endercontainers.Config;
import fr.utarwyn.endercontainers.database.Database;
import fr.utarwyn.endercontainers.database.DatabaseSet;
import fr.utarwyn.endercontainers.migration.Migration;
import fr.utarwyn.endercontainers.util.Log;
import fr.utarwyn.endercontainers.util.uuid.UUIDFetcher;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Represents the migration for 2.X versions but for a MySQL configuration.
 * @since 2.0.0
 * @author Utarwyn
 */
public class MigrationMySQL2_0 extends Migration2_0 {

	@Override
	public void perform() {
		/* ------------------- */
		/*  Format backups...  */
		/* ------------------- */
		Log.log("Backuping SQL data into file \"EnderContainers/" + this.getBackupFolder().getName() + "/mysqldump.sql\"...", true);
		if (!this.backupSQL()) return;
		Log.log("", true);

		/* ------------------- */
		/*  Format backups...  */
		/* ------------------- */
		Log.log("Reconfigure backups...", true);
		if (!this.reconfigureBackups()) return;

		/* -------------------------------- */
		/*  Format chests configuration...  */
		/* -------------------------------- */
		Log.log("Reconfigure enderchests...", true);
		if (!this.reconfigureEnderchests()) return;

		/* ------------------------- */
		/*  Format configuration...  */
		/* ------------------------- */
		Log.log("Apply old configuration...", true);
		if (!this.updateConfiguration()) return;

		/* ------------------- */
		/*  Update locales...  */
		/* ------------------- */
		Log.log("Update locales...", true);
		this.updateLocales();
	}

	/**
	 * Backups the MySQL database into a .sql file to be sure about the
	 * security of the data for the server's administrator.
	 * @return True if all data have been backuped.
	 */
	private boolean backupSQL() {
		Database db = getDatabase();
		if (db == null || db.getDumper() == null) return false;
		String dump = db.getDumper().dump();
		if (dump == null) return false;

		File file = new File(this.getBackupFolder(), "mysqldump.sql");

		try {
			PrintWriter pw = new PrintWriter(new FileOutputStream(file));
			pw.write(dump);
			pw.close();

			return true;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return false;
		}
	}

	@Override
	boolean reconfigureBackups() {
		Database db = getDatabase();
		if (db == null) return false;
		String backupTable = Config.mysqlTablePrefix + "backups";

		List<String> dataElements;
		String newCreatedBy;

		// Reformat data and created_by fields of all backups
		for (DatabaseSet backup : db.find(backupTable)) {
			String oldData = backup.getString("data");

			dataElements = new ArrayList<>();
			newCreatedBy = UUIDFetcher.getName(UUID.fromString(backup.getString("created_by")));
			if (newCreatedBy == null) newCreatedBy = "UNKNOWN";

			for (String oldChest : oldData.split(";")) {
				String[] parts = oldChest.split(":");

				dataElements.add(parts[0] + ":" + parts[6] + ":" + parts[5] + ":" + parts[1] + ":6:" + parts[4]);
			}

			// Save the updated field in the table
			if (!db.save(
					backupTable,
					DatabaseSet.makeFields(
							"data", StringUtils.join(dataElements, ";"),
							"created_by", newCreatedBy
					),
					DatabaseSet.makeConditions("id", String.valueOf(backup.getInteger("id")))
			)) return false;
		}

		return true;
	}

	@Override
	boolean reconfigureEnderchests() {
		Database db = getDatabase();
		if (db == null) return false;
		String chestsTable = Config.mysqlTablePrefix + "enderchests";
		String playersTable = Config.mysqlTablePrefix + "players";

		List<DatabaseSet> players = db.find(playersTable);
		List<DatabaseSet> chests = db.find(chestsTable);

		String owner;
		int num, rows;

		// Reformat the enderchests table
		db.dropTable(chestsTable);
		Migration.recreateMySQLTables();

		for (DatabaseSet chest : chests) {
			num = chest.getInteger("enderchest_id");
			owner = chest.getString("player_uuid");
			rows = 3;

			for (DatabaseSet player : players) {
				String uuid = player.getString("player_uuid");

				if (uuid.equals(owner)) {
					String accesses = player.getString("accesses");

					for (String chestAccess : accesses.split(";")) {
						String[] parts = chestAccess.split(":");

						if (parts[0].equals(String.valueOf(num))) {
							rows = Integer.parseInt(parts[1]);
							break;
						}
					}

					break;
				}
			}

			// Save the new enderchest in the table
			db.save(
					chestsTable, DatabaseSet.makeFields(
							"id", chest.getInteger("id"),
							"num", num,
							"owner", owner,
							"contents", chest.getString("items"),
							"rows", rows,
							"last_locking_time", chest.getTimestamp("last_save_time")
					)
			);
		}

		// Remove the old players table
		db.dropTable(playersTable);

		return true;
	}

}

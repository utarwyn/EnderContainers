package fr.utarwyn.endercontainers.database;

import fr.utarwyn.endercontainers.AbstractManager;
import fr.utarwyn.endercontainers.Config;
import fr.utarwyn.endercontainers.EnderContainers;
import fr.utarwyn.endercontainers.util.Log;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

/**
 * Class to manage the MySQL connection.
 * @since 1.0.5
 * @author Utarwyn
 */
public class MysqlManager extends AbstractManager {

	/**
	 * The chest table where to store all data of enderchests
	 */
	private static final String CHEST_TABLE = "enderchests";

	/**
	 * The backup table where to store all data of backups
	 */
	private static final String BACKUP_TABLE = "backups";

	/**
	 * The database object to perform requests
	 */
	private Database database;

	/**
	 * Used to construct the MySQL manager
	 */
	public MysqlManager() {
		super(EnderContainers.getInstance());
	}

	/**
	 * Called when the manager is initializing
	 */
	@Override
	public void initialize() {
		// MySQL is enabled or not?
		if (!Config.mysql) {
			Log.log("MySQL disabled. Using flat system to store data.", true);
			return;
		}

		long begin = System.currentTimeMillis();

		// Connect to the MySQL server ...
		this.database = new Database(Config.mysqlHost, Config.mysqlPort, Config.mysqlUser, Config.mysqlPassword, Config.mysqlDatabase);

		if (this.isReady()) {
			Log.log("MySQL enabled and ready. Connected to database " + Config.mysqlHost + ":" + Config.mysqlPort, true);
		} else {
			Config.mysql = false;
			Log.log("MySQL disabled because of an error during the connection. Now using flat system to store data.", true);
			return;
		}

		Log.log("Connection time: " + (System.currentTimeMillis() - begin) + "ms");

		// Initialize the database ...
		this.init();
	}

	/**
	 * Called when the manager is unloading
	 */
	@Override
	protected void unload() {
		try {
			if (this.isReady()) {
				this.database.close();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Allow to know if the database is ready to perform statement or not.
	 * @return True if the database is connected and ready.
	 */
	public boolean isReady() {
		return (this.database != null && this.database.isConnected());
	}

	/**
	 * Save an enderchest in the database
	 * @param insert True if the chest data has to be inserted (and not updated)
	 * @param owner The owner of the chest
	 * @param num The num of the chest
	 * @param rows The number of rows of the chest
	 * @param contents All contents of the chest
	 */
	public void saveEnderchest(boolean insert, UUID owner, int num, int rows, String contents) {
		if (insert) {
			database.save(formatTable(CHEST_TABLE), DatabaseSet.makeFields(
					"num", num,
					"owner", owner.toString(),
					"rows", rows,
					"contents", contents,
					"last_locking_time", new Timestamp(System.currentTimeMillis())
			));
		} else {
			database.save(formatTable(CHEST_TABLE), DatabaseSet.makeFields(
					"rows", rows,
					"contents", contents,
					"last_locking_time", new Timestamp(System.currentTimeMillis())
			), DatabaseSet.makeConditions(
					"num", String.valueOf(num),
					"owner", owner.toString()
			));
		}
	}

	/**
	 * List of database rows which contains all saved chests
	 * @return The list of saved enderchests
	 */
	public List<DatabaseSet> getAllEnderchests() {
		return database.find(formatTable(CHEST_TABLE));
	}

	/**
	 * Returns all enderchests stored in database of a specific player (UUID here)
	 * @param owner The owner of chests
	 * @return The list of all chests for a player
	 */
	public List<DatabaseSet> getEnderchestsOf(UUID owner) {
		return database.find(formatTable(CHEST_TABLE), DatabaseSet.makeConditions("owner", owner.toString()));
	}

	/**
	 * Drop all data from the enderchests table
	 * (Used for the applying of backups)
	 */
	public void emptyChestTable() {
		database.emptyTable(formatTable(CHEST_TABLE));
	}

	/**
	 * Save a list of enderchests in the database.
	 * @param chestSets The list of enderchests to save
	 */
	public void saveEnderchestSets(List<DatabaseSet> chestSets) {
		for (DatabaseSet set : chestSets)
			database.save(formatTable(CHEST_TABLE), set.getObjects());
	}

	/**
	 * List of database rows which contains all saved backups
	 * @return The list of saved backups
	 */
	public List<DatabaseSet> getBackups() {
		return database.find(formatTable(BACKUP_TABLE));
	}

	/**
	 * Returns a MySQL row data with a name of a backup
	 * @param name Name of a backup to get
	 * @return The found backup (or null if not found)
	 */
	public DatabaseSet getBackup(String name) {
		return database.findFirst(formatTable(BACKUP_TABLE), DatabaseSet.makeConditions("name", name));
	}

	/**
	 * Save a backup in the database
	 * @param name Name of the backup
	 * @param date Date of the backup
	 * @param type Type of the backup
	 * @param data Creation date of the backup
	 * @param createdBy Entity who created the backup (console or player)
	 */
	public void saveBackup(String name, long date, String type, String data, String createdBy) {
		database.save(formatTable(BACKUP_TABLE), DatabaseSet.makeFields(
				"name", name,
				"date", new Timestamp(date),
				"type", type,
				"data", data,
				"created_by", createdBy
		));
	}

	/**
	 * Remove a backup by its name
	 * @param name Name of the backup to remove
	 * @return True if the backup was successfully removed.
	 */
	public boolean removeBackup(String name) {
		return this.database.delete(formatTable(BACKUP_TABLE), DatabaseSet.makeConditions("name", name));
	}

	/**
	 * Initialize the database if this is the first launch of the MySQL manager
	 */
	private void init() {
		String collation = (database.getServerVersion() >= 5.5) ? "utf8mb4_unicode_ci" : "utf8_unicode_ci";
		List<String> tables = this.database.getTables();

		if (!tables.contains(formatTable(CHEST_TABLE))) {
			database.request("CREATE TABLE `" + formatTable(CHEST_TABLE) + "` (`id` INT(11) NOT NULL AUTO_INCREMENT, `num` TINYINT(2) NOT NULL DEFAULT '0', `owner` VARCHAR(36) NULL, `contents` MEDIUMTEXT NULL, `rows` INT(1) NOT NULL DEFAULT 0, `last_locking_time` TIMESTAMP NULL, PRIMARY KEY (`id`), INDEX `USER KEY` (`num`, `owner`)) COLLATE='" + collation + "' ENGINE=InnoDB;");
			Log.log("Table `" + formatTable(CHEST_TABLE) + "` created in the database.");
		}
		if (!tables.contains(formatTable(BACKUP_TABLE))) {
			database.request("CREATE TABLE `" + formatTable(BACKUP_TABLE) + "` (`id` INT(11) NOT NULL AUTO_INCREMENT, `name` VARCHAR(255) NOT NULL, `date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, `type` VARCHAR(60) NULL, `data` MEDIUMTEXT NULL, `created_by` VARCHAR(60) NULL, PRIMARY KEY (`id`)) COLLATE='" + collation + "' ENGINE=InnoDB;");
			Log.log("Table `" + formatTable(BACKUP_TABLE) + "` created in the database.");
		}
	}

	/**
	 * Format a table's name with the prefix.
	 * @param table The name of the table to format
	 * @return The formatted name of the table
	 */
	private String formatTable(String table) {
		return Config.mysqlTablePrefix + table;
	}

}

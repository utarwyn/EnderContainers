package fr.utarwyn.endercontainers.database;

import fr.utarwyn.endercontainers.AbstractManager;
import fr.utarwyn.endercontainers.configuration.Files;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

/**
 * Class to manage the MySQL connection.
 *
 * @author Utarwyn
 * @since 1.0.5
 */
public class DatabaseManager extends AbstractManager {

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
	 * Escape a list of fields to be sure that all requests are
	 * compliants with all SQL servers.
	 *
	 * @param fields Fields to wrap with quotes (to escape)
	 * @return Escaped fields, columns or table names
	 */
	public static String[] escapeFieldArray(String[] fields) {
		for (int i = 0; i < fields.length; i++) {
			fields[i] = '`' + fields[i] + '`';
		}
		return fields;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void load() {
		// MySQL is enabled or not?
		if (!Files.getConfiguration().isMysql()) {
			this.logger.info("MySQL disabled. Using flat system to store data.");
			return;
		}

		long begin = System.currentTimeMillis();

		// Connect to the MySQL server ...
		String host = Files.getConfiguration().getMysqlHost();
		int port = Files.getConfiguration().getMysqlPort();

		this.database = new Database(
				host, port,
				Files.getConfiguration().getMysqlUser(),
				Files.getConfiguration().getMysqlPassword(),
				Files.getConfiguration().getMysqlDatabase()
		);

		if (this.database.isConnected()) {
			this.logger.log(Level.INFO, "MySQL enabled and ready. Connected to database {0}:{1}",
					new Object[]{host, String.valueOf(port)});

			if (Files.getConfiguration().isDebug()) {
				this.logger.log(Level.INFO, "Connection time: {0}ms", System.currentTimeMillis() - begin);
			}

			this.init();
		} else {
			this.logger.severe("Unable to connect to your database. Please verify your credentials.");
			this.logger.warning("SQL supports disabled because of an error during the connection.");
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void unload() {
		try {
			if (this.database != null) {
				this.database.close();
			}
		} catch (SQLException e) {
			this.logger.log(Level.SEVERE, "Cannot close the database connection.", e);
		}
	}

	/**
	 * Allow to know if the database is ready to perform statement or not.
	 *
	 * @return True if the database is connected and ready.
	 */
	public boolean isReady() {
		return this.database != null && this.database.isConnected();
	}

	/**
	 * Save an enderchest in the database
	 *
	 * @param insert   True if the chest data has to be inserted (and not updated)
	 * @param owner    The owner of the chest
	 * @param num      The num of the chest
	 * @param rows     The number of rows of the chest
	 * @param contents All contents of the chest
	 */
	public void saveEnderchest(boolean insert, UUID owner, int num, int rows, String contents) throws SQLException {
		Timestamp now = new Timestamp(System.currentTimeMillis());

		if (insert) {
			database.update(formatTable(CHEST_TABLE))
					.fields("num", "owner", "rows", "contents", "last_locking_time")
					.values(num, owner.toString(), rows, contents, now)
					.execute();
		} else {
			database.update(formatTable(CHEST_TABLE))
					.fields("rows", "contents", "last_locking_time")
					.values(rows, contents, now)
					.where("`num` = ?", "`owner` = ?")
					.attributes(String.valueOf(num), owner.toString())
					.execute();
		}
	}

	/**
	 * List of database rows which contains all saved chests
	 *
	 * @return The list of saved enderchests
	 */
	public List<DatabaseSet> getAllEnderchests() {
		try {
			return database.select().from(formatTable(CHEST_TABLE)).findAll();
		} catch (SQLException e) {
			this.logger.log(Level.SEVERE, "Cannot retreive all enderchests from the database", e);
			return new ArrayList<>();
		}
	}

	/**
	 * Returns all enderchests stored in database of a specific player (UUID here)
	 *
	 * @param owner The owner of chests
	 * @return The list of all chests for a player
	 */
	public List<DatabaseSet> getEnderchestsOf(UUID owner) {
		try {
			return database.select().from(formatTable(CHEST_TABLE))
					.where("`owner` = ?").attributes(owner.toString())
					.findAll();
		} catch (SQLException e) {
			this.logger.log(Level.SEVERE,
					"Cannot retreive enderchests of user " + owner + " from the database", e);
			return new ArrayList<>();
		}
	}

	/**
	 * Drop all data from the enderchests table
	 * (Used for the applying of backups)
	 */
	public void emptyChestTable() throws SQLException {
		database.emptyTable(formatTable(CHEST_TABLE));
	}

	/**
	 * Save a list of enderchests in the database.
	 *
	 * @param chestSets The list of enderchests to save
	 */
	public void saveEnderchestSets(List<DatabaseSet> chestSets) throws SQLException {
		for (DatabaseSet set : chestSets) {
			database.update(formatTable(CHEST_TABLE))
					.fields(set.getKeys().toArray(new String[0]))
					.values(set.getValues())
					.execute();
		}
	}

	/**
	 * List of database rows which contains all saved backups
	 *
	 * @return The list of saved backups
	 */
	public List<DatabaseSet> getBackups() {
		try {
			return database.select().from(formatTable(BACKUP_TABLE)).findAll();
		} catch (SQLException e) {
			this.logger.log(Level.SEVERE, "Cannot retreive backups list from the database", e);
			return new ArrayList<>();
		}
	}

	/**
	 * Returns a MySQL row data with a name of a backup
	 *
	 * @param name Name of a backup to get
	 * @return The found backup (or null if not found)
	 */
	public DatabaseSet getBackup(String name) {
		try {
			return database.select().from(formatTable(BACKUP_TABLE))
					.where("`name` = ?").attributes(name)
					.find();
		} catch (SQLException e) {
			this.logger.log(Level.SEVERE,
					"Cannot retreive the backup " + name + " from the database", e);
			return null;
		}
	}

	/**
	 * Save a backup in the database
	 *
	 * @param name      Name of the backup
	 * @param date      Date of the backup
	 * @param type      Type of the backup
	 * @param data      Creation date of the backup
	 * @param createdBy Entity who created the backup (console or player)
	 */
	public void saveBackup(String name, long date, String type, String data, String createdBy) throws SQLException {
		database.update(formatTable(BACKUP_TABLE))
				.fields("name", "date", "type", "data", "created_by")
				.values(name, new Timestamp(date), type, data, createdBy)
				.execute();
	}

	/**
	 * Remove a backup by its name
	 *
	 * @param name Name of the backup to remove
	 * @return True if the backup was successfully removed.
	 */
	public boolean removeBackup(String name) {
		try {
			return this.database.delete("`name` = ?").from(formatTable(BACKUP_TABLE))
					.attributes(name).execute();
		} catch (SQLException e) {
			this.logger.log(Level.SEVERE, "Cannot delete backup " + name + " from the database", e);
			return false;
		}
	}

	/**
	 * Initialize the database if this is the first launch of the MySQL manager
	 */
	private void init() {
		try {
			String collation = this.database.getServerVersion() >= 5.5 ? "utf8mb4_unicode_ci" : "utf8_unicode_ci";
			List<String> tables = this.database.getTables();

			if (!tables.contains(formatTable(CHEST_TABLE))) {
				database.request("CREATE TABLE `" + formatTable(CHEST_TABLE) + "` (`id` INT(11) NOT NULL AUTO_INCREMENT, `num` TINYINT(2) NOT NULL DEFAULT '0', `owner` VARCHAR(36) NULL, `contents` MEDIUMTEXT NULL, `rows` INT(1) NOT NULL DEFAULT 0, `last_locking_time` TIMESTAMP NULL, PRIMARY KEY (`id`), INDEX `USER KEY` (`num`, `owner`)) COLLATE='" + collation + "' ENGINE=InnoDB;");
				if (Files.getConfiguration().isDebug()) {
					this.logger.info(String.format("Table `%s` created in the database.", formatTable(CHEST_TABLE)));
				}
			}
			if (!tables.contains(formatTable(BACKUP_TABLE))) {
				database.request("CREATE TABLE `" + formatTable(BACKUP_TABLE) + "` (`id` INT(11) NOT NULL AUTO_INCREMENT, `name` VARCHAR(255) NOT NULL, `date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, `type` VARCHAR(60) NULL, `data` MEDIUMTEXT NULL, `created_by` VARCHAR(60) NULL, PRIMARY KEY (`id`)) COLLATE='" + collation + "' ENGINE=InnoDB;");
				if (Files.getConfiguration().isDebug()) {
					this.logger.info(String.format("Table `%s` created in the database.", formatTable(BACKUP_TABLE)));
				}
			}
		} catch (SQLException e) {
			this.logger.log(Level.SEVERE, "Cannot create tables in the database", e);
		}
	}

	/**
	 * Format a table's name with the prefix.
	 *
	 * @param table The name of the table to format
	 * @return The formatted name of the table
	 */
	private String formatTable(String table) {
		return Files.getConfiguration().getMysqlTablePrefix() + table;
	}

}

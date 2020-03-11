package fr.utarwyn.endercontainers.database;

import fr.utarwyn.endercontainers.AbstractManager;
import fr.utarwyn.endercontainers.configuration.Configuration;
import fr.utarwyn.endercontainers.configuration.Files;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
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
    public static String[] espaceFields(String[] fields) {
        return Arrays.stream(fields)
                .map(field -> '`' + field + '`')
                .toArray(String[]::new);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void load() {
        // MySQL is enabled or not?
        if (Files.getConfiguration().isMysql()) {
            // Setup the database from the configuration
            this.setupDatabase();

            try {
                long beginTime = System.currentTimeMillis();

                // Open a new connection to the SQL server
                this.database.open();

                // Initialize the tables if needed
                this.createTables();

                // Log the successful connection into the console
                this.logConnection(beginTime);
            } catch (SQLException e) {
                this.logger.log(Level.SEVERE, "Unable to connect to your database. Please verify your credentials.", e);
                this.logger.warning("SQL supports disabled because of an error during the connection.");
                this.database = null;
            }
        } else {
            this.logger.info("MySQL disabled. Using flat system to store data.");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected synchronized void unload() {
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
        if (insert) {
            this.database.update(formatTable(CHEST_TABLE))
                    .fields("num", "owner", "rows", "contents")
                    .values(num, owner.toString(), rows, contents)
                    .execute();
        } else {
            this.database.update(formatTable(CHEST_TABLE))
                    .fields("rows", "contents")
                    .values(rows, contents)
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
    public List<DatabaseSet> getAllEnderchests() throws SQLException {
        return this.database.select()
                .from(formatTable(CHEST_TABLE))
                .findAll();
    }

    /**
     * Returns all enderchests stored in database of a specific player (UUID here)
     *
     * @param owner The owner of chests
     * @return The list of all chests for a player
     */
    public List<DatabaseSet> getEnderchestsOf(UUID owner) throws SQLException {
        return this.database.select().from(formatTable(CHEST_TABLE))
                .where("`owner` = ?").attributes(owner.toString())
                .findAll();
    }

    /**
     * Replace all enderchests in the table
     * by a list of new enderchests.
     *
     * @param datasets The list of enderchests to save
     */
    public void replaceEnderchests(List<DatabaseSet> datasets) throws SQLException {
        this.database.emptyTable(formatTable(CHEST_TABLE));

        for (DatabaseSet set : datasets) {
            this.database.update(formatTable(CHEST_TABLE))
                    .fields(set.getKeys().toArray(new String[0]))
                    .values(set.getValues().toArray(new Object[0]))
                    .execute();
        }
    }

    /**
     * List of database rows which contains all saved backups
     *
     * @return The list of saved backups
     */
    public List<DatabaseSet> getBackups() throws SQLException {
        return this.database.select().from(formatTable(BACKUP_TABLE)).findAll();
    }

    /**
     * Returns a MySQL row data with a name of a backup
     *
     * @param name Name of a backup to get
     * @return The found backup (or null if not found)
     */
    public Optional<DatabaseSet> getBackup(String name) throws SQLException {
        return Optional.ofNullable(this.database.select()
                .from(formatTable(BACKUP_TABLE))
                .where("`name` = ?").attributes(name)
                .find());
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
        this.database.update(formatTable(BACKUP_TABLE))
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
    public boolean removeBackup(String name) throws SQLException {
        Optional<DatabaseSet> backup = this.getBackup(name);

        return backup.isPresent() && this.database.delete("`id` = ?")
                .from(formatTable(BACKUP_TABLE))
                .attributes(backup.get().getInteger("id"))
                .execute();
    }

    /**
     * Setup the database connection from user configuration.
     */
    private void setupDatabase() {
        Configuration config = Files.getConfiguration();

        // Setup the database object
        this.database = new Database(
                config.getMysqlHost(), config.getMysqlPort(),
                config.getMysqlUser(), config.getMysqlPassword(), config.getMysqlDatabase()
        );

        // Connection over SSL?
        if (config.isMysqlSsl()) {
            DatabaseSecureCredentials credentials = new DatabaseSecureCredentials();

            try {
                credentials.setClientKeystore(config.getMysqlSslKeystoreFile(), config.getMysqlSslKeystorePassword());
            } catch (NullPointerException e) {
                this.logger.log(Level.SEVERE, "keystore file or password from your configuration seems to be null", e);
                return;
            }

            if (config.getMysqlSslTrustKeystoreFile() != null && config.getMysqlSslTrustKeystorePassword() != null) {
                credentials.setTrustKeystore(config.getMysqlSslTrustKeystoreFile(), config.getMysqlSslTrustKeystorePassword());
            }

            this.database.setSecureCredentials(credentials);
        }
    }

    /**
     * Log a successful database connection into the console.
     *
     * @param beginTime timestamp where the connection has started
     */
    private void logConnection(long beginTime) {
        this.logger.log(Level.INFO, "MySQL enabled and ready. Connected to database {0}", this.database.getEndpoint());

        if (this.database.isSecured()) {
            this.logger.info("Good news! You are using a secure connection to your database server.");
        } else {
            this.logger.warning("You are using an unsecure connection to your database server. Be careful!");
        }

        if (Files.getConfiguration().isDebug()) {
            this.logger.log(Level.INFO, "Connection time: {0}ms", System.currentTimeMillis() - beginTime);
        }
    }

    /**
     * Initialize the database if this is the first launch of the MySQL manager
     */
    private void createTables() throws SQLException {
        String collation = this.database.getServerVersion() >= 5.5 ? "utf8mb4_unicode_ci" : "utf8_unicode_ci";
        List<String> tables = this.database.getTables();

        if (!tables.contains(formatTable(CHEST_TABLE))) {
            database.request("CREATE TABLE `" + formatTable(CHEST_TABLE) + "` (`id` INT(11) NOT NULL AUTO_INCREMENT, `num` TINYINT(2) NOT NULL DEFAULT '0', `owner` VARCHAR(36) NULL, `contents` MEDIUMTEXT NULL, `rows` INT(1) NOT NULL DEFAULT 0, PRIMARY KEY (`id`), UNIQUE KEY `NUM OWNER` (`num`,`owner`), INDEX `USER KEY` (`num`, `owner`)) COLLATE='" + collation + "' ENGINE=InnoDB;");
            if (Files.getConfiguration().isDebug()) {
                this.logger.log(Level.INFO, "Table `{0}` created in the database.", formatTable(CHEST_TABLE));
            }
        }
        if (!tables.contains(formatTable(BACKUP_TABLE))) {
            database.request("CREATE TABLE `" + formatTable(BACKUP_TABLE) + "` (`id` INT(11) NOT NULL AUTO_INCREMENT, `name` VARCHAR(255) NOT NULL, `date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, `type` VARCHAR(60) NULL, `data` MEDIUMTEXT NULL, `created_by` VARCHAR(60) NULL, PRIMARY KEY (`id`)) COLLATE='" + collation + "' ENGINE=InnoDB;");
            if (Files.getConfiguration().isDebug()) {
                this.logger.log(Level.INFO, "Table `{0}` created in the database.", formatTable(BACKUP_TABLE));
            }
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

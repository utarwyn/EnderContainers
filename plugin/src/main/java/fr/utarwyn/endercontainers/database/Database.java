package fr.utarwyn.endercontainers.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.pool.HikariPool;
import fr.utarwyn.endercontainers.configuration.Configuration;
import fr.utarwyn.endercontainers.configuration.Files;
import fr.utarwyn.endercontainers.database.adapter.DatabaseAdapter;
import fr.utarwyn.endercontainers.database.request.DeleteRequest;
import fr.utarwyn.endercontainers.database.request.Request;
import fr.utarwyn.endercontainers.database.request.SavingRequest;
import fr.utarwyn.endercontainers.database.request.SelectRequest;

import java.sql.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Manages connections to a SQL database.
 *
 * @author Utarwyn <maximemalgorn@gmail.com>
 * @since 1.0.5
 */
public class Database implements AutoCloseable {

    /**
     * Configuration object used to initialize the connection pool.
     */
    private final HikariConfig configuration;
    /**
     * Source object used to perform requests to the database.
     */
    HikariDataSource source;

    /**
     * Stores the database server url.
     */
    private final String serverUrl;

    /**
     * Has the connection been opened over SSL?
     */
    private final boolean secure;

    /**
     * Constructs this object to send request to a database.
     *
     * @param adapter     object to configure the connection pool
     * @param credentials credentials object to connect over SSL
     */
    Database(DatabaseAdapter adapter, DatabaseSecureCredentials credentials) {
        this.configuration = new HikariConfig();

        // Retrieve the database server url from the adapter
        Configuration pluginConfig = Files.getConfiguration();
        this.serverUrl = adapter.getServerUrl(pluginConfig);

        // Apply the plugin configuration to the source config object
        String sourceUrl = adapter.getSourceUrl(this.serverUrl, pluginConfig.getMysqlDatabase());
        this.configuration.setJdbcUrl(sourceUrl);
        adapter.configure(this.configuration, pluginConfig);

        // Apply secure credentials if provided
        if (credentials != null) {
            credentials.apply(this.configuration);
            this.secure = true;
        } else {
            this.secure = false;
        }
    }

    /**
     * Retrieves the database server url where the source is connected.
     *
     * @return connected server url
     */
    public String getServerUrl() {
        return serverUrl;
    }

    /**
     * Checks if the data source is running or not.
     *
     * @return true if the source is running, false otherwise
     */
    public boolean isRunning() {
        return this.source != null && this.source.isRunning();
    }

    /**
     * Checks if the connection has been opened over SSL or not.
     *
     * @return true if the connection is secured
     */
    public boolean isSecure() {
        return this.secure;
    }

    /**
     * Initialize the connection pool from the registered configuration.
     * If the pool has already been initialized, does nothing.
     *
     * @throws DatabaseConnectException thrown if the connection pool cannot connect to the database
     */
    public void initialize() throws DatabaseConnectException {
        if (this.source == null) {
            try {
                this.source = new HikariDataSource(this.configuration);
            } catch (HikariPool.PoolInitializationException e) {
                throw new DatabaseConnectException(e);
            }
        }
    }

    /**
     * Closes the connection pool to the database server.
     */
    @Override
    public void close() {
        if (this.isRunning()) {
            this.source.close();
        }
    }

    /**
     * Returns the version description of the connected SQL server
     *
     * @return Version of the database server
     */
    public Double getServerVersion() throws SQLException {
        try (Connection connection = this.source.getConnection()) {
            String version = connection.getMetaData().getDatabaseProductVersion();

            if (version.indexOf('-') > -1) {
                version = version.split("-")[0];
            }

            int pointIndex = version.lastIndexOf('.') == -1 ? version.length() - 1 : version.lastIndexOf('.');
            return Double.valueOf(version.substring(0, pointIndex));
        }
    }

    /**
     * Returns a collection with all tables of the database.
     *
     * @return collection of table names
     */
    public Set<String> getTables() throws SQLException {
        Set<String> tables = new HashSet<>();

        try (Connection conn = this.source.getConnection();
             ResultSet result = conn.getMetaData().getTables(null, null, "%", null)) {
            while (result.next()) {
                tables.add(result.getString(3));
            }
        }

        return tables;
    }

    /**
     * Select data on a specific table
     *
     * @param fields Fields to be selected
     * @return The Request object
     */
    public SelectRequest select(String... fields) {
        return new SelectRequest(this, fields);
    }

    /**
     * Update a table with new rows or edit some of them.
     *
     * @param table Table to be updated
     * @return The Request object
     */
    public SavingRequest update(String table) {
        return new SavingRequest(this, table);
    }

    /**
     * Create a delete request with conditions to be executed on a table.
     *
     * @param conditions Conditions to perform the delete request
     * @return The Request object
     */
    public DeleteRequest delete(String... conditions) {
        return new DeleteRequest(this, conditions);
    }

    /**
     * Send a custom request into the database.
     *
     * @param request The special request to execute!
     */
    public void request(String request) throws SQLException {
        try (Connection connection = this.source.getConnection();
             Statement statement = connection.createStatement()) {
            statement.executeUpdate(request);
        }
    }

    /**
     * Execute a select request on the database.
     * This method must be called from a Request object.
     *
     * @param request Request to execute
     * @return Result of the processed request
     * @throws SQLException if a SQL exception has been thrown during the process
     */
    public List<DatabaseSet> execQueryStatement(SelectRequest request) throws SQLException {
        Object[] attributes = request.getAttributes();

        try (Connection connection = this.source.getConnection();
             PreparedStatement st = connection.prepareStatement(request.getRequest())) {

            for (int i = 1; i <= attributes.length; i++) {
                st.setObject(i, attributes[i - 1]);
            }

            try (ResultSet resultSet = st.executeQuery()) {
                return DatabaseSet.resultSetToDatabaseSet(resultSet);
            }
        }
    }

    /**
     * Execute an update statement on the database.
     * This method must be called from a Request object.
     *
     * @param request Request object to manage
     * @return True if the update statement was executed
     * @throws SQLException if a SQL exception has been thrown during the process
     */
    public boolean execUpdateStatement(Request request) throws SQLException {
        Object[] attributes = request.getAttributes();

        try (Connection connection = this.source.getConnection();
             PreparedStatement statement = connection.prepareStatement(request.getRequest())) {
            for (int i = 1; i <= attributes.length; i++) {
                statement.setObject(i, attributes[i - 1]);
            }

            return statement.executeUpdate() > 0;
        }
    }

}

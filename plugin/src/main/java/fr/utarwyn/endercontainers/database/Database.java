package fr.utarwyn.endercontainers.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import fr.utarwyn.endercontainers.configuration.Configuration;
import fr.utarwyn.endercontainers.configuration.Files;
import fr.utarwyn.endercontainers.database.driver.DatabaseDriver;
import fr.utarwyn.endercontainers.database.request.DeleteRequest;
import fr.utarwyn.endercontainers.database.request.Request;
import fr.utarwyn.endercontainers.database.request.SavingRequest;
import fr.utarwyn.endercontainers.database.request.SelectRequest;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages connections to a SQL database.
 *
 * @author Utarwyn <maximemalgorn@gmail.com>
 * @since 1.0.5
 */
public class Database implements AutoCloseable {

    /**
     * Source object used to perform requests to the database
     */
    private final HikariDataSource source;

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
     * @param driver object to configure the connection pool
     */
    Database(DatabaseDriver driver, DatabaseSecureCredentials credentials) {
        Configuration pluginConfig = Files.getConfiguration();
        HikariConfig sourceConfig = new HikariConfig();

        // Retrieve the database server url from the driver
        this.serverUrl = driver.getServerUrl(pluginConfig);

        // Apply the plugin configuration to the source config object
        String sourceUrl = driver.getSourceUrl(this.serverUrl, pluginConfig.getMysqlDatabase());
        sourceConfig.setJdbcUrl(sourceUrl);
        driver.configure(sourceConfig, pluginConfig);

        // Apply secure credentials if provided
        if (credentials != null) {
            credentials.apply(sourceConfig);
            this.secure = true;
        } else {
            this.secure = false;
        }

        // Create the source object
        this.source = new HikariDataSource(sourceConfig);
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
        return this.source.isRunning();
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
     * Checks if the source can connect to the database or not.
     */
    public void testConnection() throws SQLException {
        this.source.getConnection().close();
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
     * Returns the list of all tables created in the database
     *
     * @return List of tables.
     */
    public List<String> getTables() throws SQLException {
        List<String> tables = new ArrayList<>();

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

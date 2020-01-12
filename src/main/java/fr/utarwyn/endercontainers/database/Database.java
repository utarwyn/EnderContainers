package fr.utarwyn.endercontainers.database;

import fr.utarwyn.endercontainers.database.request.DeleteRequest;
import fr.utarwyn.endercontainers.database.request.IRequest;
import fr.utarwyn.endercontainers.database.request.SavingRequest;
import fr.utarwyn.endercontainers.database.request.SelectRequest;
import org.apache.commons.dbcp2.BasicDataSource;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Class used to manage a remote SQL database.
 * <p>
 * Requests model is inspired from my UtariaDatabase plugin.
 * (you can see the source code here: https://github.com/Utaria/UtariaDatabase)
 *
 * @author Utarwyn
 * @since 1.0.5
 */
public class Database implements AutoCloseable {

    /**
     * Host of the MySQL server
     */
    private String host;

    /**
     * Port of the MySQL server
     */
    private int port;

    /**
     * User used to connect to the MySQL server
     */
    private String user;

    /**
     * Password used to connect to the MySQL server
     */
    private String password;

    /**
     * Name of the database used to store data
     */
    private String name;

    /**
     * Source object used to perform requests to the database
     */
    private BasicDataSource source;

    /**
     * Constructor to create a new MySQL database object.
     *
     * @param host     host of the database server
     * @param port     port of the database server
     * @param user     username to open the connection
     * @param password password to open the connection
     * @param name     name of the database to use
     */
    Database(String host, int port, String user, String password, String name) {
        this.host = host;
        this.port = port;
        this.user = user;
        this.password = password;
        this.name = name;

        try {
            this.createPool();
        } catch (SQLException ex) {
            this.source = null;
        }
    }

    /**
     * Know if the object is connected to the database or not.
     *
     * @return True if connected.
     */
    public boolean isConnected() {
        return this.source != null && !this.source.isClosed();
    }

    /**
     * Drop all data from a specific table passed in parameter
     *
     * @param tableName The name of the table to empty
     */
    public void emptyTable(String tableName) throws SQLException {
        try (Connection connection = this.source.getConnection();
             Statement statement = connection.createStatement()) {
            statement.executeUpdate("USE `" + this.name + "`");
            statement.executeUpdate("SET SQL_SAFE_UPDATES = 0;");
            statement.executeUpdate("truncate `" + tableName + "`");
            statement.executeUpdate("SET SQL_SAFE_UPDATES = 1;");
        }
    }

    /**
     * Closes the connection to the SQL server
     *
     * @throws SQLException throwed if the connection cannot be closed.
     */
    @Override
    public void close() throws SQLException {
        if (this.isConnected()) {
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
    public boolean execUpdateStatement(IRequest request) throws SQLException {
        Object[] attributes = request.getAttributes();

        try (Connection connection = this.source.getConnection();
             PreparedStatement statement = connection.prepareStatement(request.getRequest())) {
            for (int i = 1; i <= attributes.length; i++) {
                statement.setObject(i, attributes[i - 1]);
            }

            return statement.executeUpdate() > 0;
        }
    }

    /**
     * Initialize the external pool SQL object
     * (the object executes all requests in an optimized and intelligent thread)
     */
    private void createPool() throws SQLException {
        source = new BasicDataSource();
        source.setDriverClassName("com.mysql.jdbc.Driver");
        source.setUrl("jdbc:mysql://" + this.host + ":" + this.port + "/" + this.name + "?useSSL=false");
        source.setUsername(this.user);
        source.setPassword(this.password);

        source.setInitialSize(1);
        source.setMaxOpenPreparedStatements(8);
        source.setMaxTotal(8);

        // Connection test
        source.getConnection().close();
    }

}

package fr.utarwyn.endercontainers.database;

import fr.utarwyn.endercontainers.Config;
import fr.utarwyn.endercontainers.database.request.DeleteRequest;
import fr.utarwyn.endercontainers.database.request.IRequest;
import fr.utarwyn.endercontainers.database.request.SavingRequest;
import fr.utarwyn.endercontainers.database.request.SelectRequest;
import fr.utarwyn.endercontainers.util.Log;
import org.apache.commons.dbcp2.BasicDataSource;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Class used to manage a remote SQL database.
 *
 * Requests model is inspired from my UtariaDatabase plugin.
 * (you can see the source code here: https://github.com/Utaria/UtariaDatabase)
 *
 * @since 1.0.5
 * @author Utarwyn
 */
public class Database {

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
	 * Database used to store data
	 */
	private String database;

	/**
	 * Source object used to perform requests to the database
	 */
	private BasicDataSource source;

	/**
	 * An instance of the MySQL dumper
	 */
	private MysqlDumper dumper;

	/**
	 * Constructor used to create a MySQL database object
	 * @param host Host of the MySQL server
	 * @param port Port of the MySQL server
	 * @param user Username to connect to the database
	 * @param password Password to connect to the database
	 * @param database The name of the database
	 */
	Database(String host, int port, String user, String password, String database) {
		this.host = host;
		this.port = port;
		this.user = user;
		this.password = password;
		this.database = database;

		this.createPool();
	}

	/**
	 * Know if the object is connected to the database or not.
	 * @return True if connected.
	 */
	public boolean isConnected() {
		try {
			return (this.source != null && !this.source.isClosed());
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * Get the dumper linked to this database. Null if the connection cannot be established.
	 * @return The MySQL dumper
	 */
	public MysqlDumper getDumper() {
		return this.dumper;
	}

	/**
	 * Drop all data from a specific table passed in parameter
	 * @param tableName The name of the table to empty
	 */
	public void emptyTable(String tableName) {
		Statement s = null;
		Connection conn = null;

		try {
			conn = getConnection();
			assert conn != null;
			s = conn.createStatement();

			s.executeUpdate("USE " + this.database);
			s.executeUpdate("SET SQL_SAFE_UPDATES=0;");
			s.executeUpdate("truncate " + tableName);
			s.executeUpdate("SET SQL_SAFE_UPDATES=1;");

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			closeConnection(conn);
			closeStatement(s);
		}
	}

	/**
	 * Drop a specific table passed in parameter
	 * @param tableName The name of the table to drop
	 */
	public void dropTable(String tableName) {
		Statement s = null;
		Connection conn = null;

		try {
			conn = getConnection();
			assert conn != null;
			s = conn.createStatement();

			s.executeUpdate("USE `" + this.database + "`");
			s.executeUpdate("DROP TABLE `" + tableName + "`");
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			closeConnection(conn);
			closeStatement(s);
		}
	}

	/**
	 * Closes the connection to the MySQL server
	 * @throws SQLException Exception throwed if the connection cannot be closed.
	 */
	public void close() throws SQLException {
		this.source.close();
	}

	/**
	 * Returns the version description of the connected SQL server
	 * @return Version of the database server
	 */
	public Double getServerVersion() {
		Connection conn = null;
		String version;

		try {
			conn = getConnection();
			version = isConnected() ? conn.getMetaData().getDatabaseProductVersion() : "0.0";

			if (version.indexOf("-") > 0) {
				version = version.split("-")[0];
			}

			int pointIndex = version.lastIndexOf('.') == -1 ? version.length() - 1 : version.lastIndexOf('.');
			return Double.valueOf(version.substring(0, pointIndex));
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		} finally {
			closeConnection(conn);
		}
	}

	/**
	 * Returns the list of all tables created in the database
	 * @return List of tables.
	 */
	public List<String> getTables() {
		Connection conn = null;
		List<String> tables = new ArrayList<>();

		try {
			conn = getConnection();
			ResultSet result = conn.getMetaData().getTables(null, null, "%", null);

			while (result.next()) {
				tables.add(result.getString(3));
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			closeConnection(conn);
		}

		return tables;
	}

	/**
	 * Select data on a specific table
	 * @param fields Fields to be selected
	 * @return The Request object
	 */
	public SelectRequest select(String... fields) {
		return new SelectRequest(this, fields);
	}

	/**
	 * Update a table with new rows or edit some of them.
	 * @param table Table to be updated
	 * @return The Request object
	 */
	public SavingRequest update(String table) {
		return new SavingRequest(this, table);
	}

	/**
	 * Create a delete request with conditions to be executed on a table.
	 * @param conditions Conditions to perform the delete request
	 * @return The Request object
	 */
	public DeleteRequest delete(String... conditions) {
		return new DeleteRequest(this, conditions);
	}

	/**
	 * Send a custom request into the database.
	 * @param request The special request to execute!
	 */
	public void request(String request) {
		Connection conn = null;
		Statement s = null;

		try {
			conn = getConnection();
			s = conn.createStatement();
			Log.log("Executing request '" + request + "'");
			s.executeUpdate(request);
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			closeStatement(s);
			closeConnection(conn);
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
		Connection conn = this.getConnection();
		if (conn == null) return null;

		Object[] attributes = request.getAttributes();
		PreparedStatement st = conn.prepareStatement(request.getRequest());

		for (int i = 1; i <= attributes.length; i++) {
			st.setObject(i, attributes[i - 1]);
		}

		return DatabaseSet.resultSetToDatabaseSet(st.executeQuery());
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
		Connection conn = this.getConnection();
		if (conn == null) return false;

		// Si la connexion est en lecture seule, on interdit l'écriture dans la base.
		Object[] attributes = request.getAttributes();
		PreparedStatement st = conn.prepareStatement(request.getRequest());

		for (int i = 1; i <= attributes.length; i++) {
			st.setObject(i, attributes[i - 1]);
		}

		return st.executeUpdate() > 0;
	}

	/**
	 * Returns the connection created by the source pool object.
	 * @return The MySQL Connection object.
	 */
	Connection getConnection() {
		try {
			return (this.source != null) ? this.source.getConnection() : null;
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Initialize the external pool SQL object
	 * (the object executes all requests in an optimized and intelligent thread)
	 */
	private void createPool() {
		try {
			source = new BasicDataSource();
			source.setDriverClassName("com.mysql.jdbc.Driver");
			source.setUrl("jdbc:mysql://" + this.host + ":" + this.port + "/" + this.database);
			source.setUsername(this.user);
			source.setPassword(this.password);

			source.setInitialSize(1);
			source.setMaxOpenPreparedStatements(8);
			source.setMaxTotal(8);

			// Connection test
			Connection conn = source.getConnection();
			conn.close();

			// Initialize the dumper linked to this database
			this.dumper = new MysqlDumper(this);
		} catch (Exception e) {
			this.source = null;
			Log.error("Mysql error: unable to connect to the database at " + this.host + ":" + this.port + ". Please verify your credentials.");

			Config.mysql = false;
			Config.enabled = true;
		}
	}

	/**
	 * Method used to close a statement without to manage the exception each times.
	 * @param statement The statement to properly close
	 */
	private void closeStatement(Statement statement) {
		try {
			if (statement != null)
				statement.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Method used to close a MySQL connection without to manage the exception each times.
	 * @param conn The connection to properly close
	 */
	private void closeConnection(Connection conn) {
		try {
			if (conn != null)
				conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}

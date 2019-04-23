package fr.utarwyn.endercontainers.database;

import fr.utarwyn.endercontainers.Config;
import fr.utarwyn.endercontainers.util.Log;
import org.apache.commons.dbcp2.BasicDataSource;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Class used to manage a remote MySQL database
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
	 * Find data in the database
	 * @param table The table to start the selection
	 * @return List of all rows selected in the specified table.
	 */
	public List<DatabaseSet> find(String table) {
		return this.find(table, null);
	}

	/**
	 * Find data in the database
	 * @param table The table to start the selection
	 * @param conditions A map which represents "WHERE" in SQL
	 * @return List of all rows selected in the specified table.
	 */
	public List<DatabaseSet> find(String table, Map<String, String> conditions) {
		return this.find(table, conditions, null);
	}

	/**
	 * Find data in the database
	 * @param table The table to start the selection
	 * @param conditions A map which represents "WHERE" in SQL
	 * @param orderby A list which represents "ORDER BY" in SQL
	 * @return List of all rows selected in the specified table.
	 */
	public List<DatabaseSet> find(String table, Map<String, String> conditions, List<String> orderby) {
		return this.find(table, conditions, orderby, null);
	}

	/**
	 * Find data in the database
	 * @param table The table to start the selection
	 * @param conditions A map which represents "WHERE" in SQL
	 * @param orderby A list which represents "ORDER BY" in SQL
	 * @param fields A list which includes all fields that will be returned by the request
	 * @return List of all rows selected in the specified table.
	 */
	public List<DatabaseSet> find(String table, Map<String, String> conditions, List<String> orderby, List<String> fields) {
		return find(table, conditions, orderby, fields, null);
	}

	/**
	 * Find data in the database
	 * @param table The table to start the selection
	 * @param conditions A map which represents "WHERE" in SQL
	 * @param orderby A list which represents "ORDER BY" in SQL
	 * @param fields A list which includes all fields that will be returned by the request
	 * @param limit Used to precise a SQL limitation for the prepared statement
	 * @return List of all rows selected in the specified table.
	 */
	public List<DatabaseSet> find(String table, Map<String, String> conditions, List<String> orderby, List<String> fields, List<Integer> limit) {
		List<DatabaseSet> result = null;
		PreparedStatement statement = null;
		Connection conn = null;

		if (!isConnected()) return null;

		StringBuilder strFields = new StringBuilder();
		if (fields != null) {
			for (String field : fields) {
				strFields.append(field).append(",");
			}
			strFields = new StringBuilder(strFields.substring(0, strFields.length() - 1));
		} else {
			strFields.append('*');
		}

		// Format fields & elements
		StringBuilder request = new StringBuilder();
		List<Object> objects = new ArrayList<>();

		request.append("SELECT ").append(strFields).append(" FROM `").append(table).append('`');
		this.prepareRequestWhere(request, conditions, objects);

		if (orderby != null && orderby.size() == 2) {
			request.append(" ORDER BY `").append(orderby.get(0)).append("` ").append(orderby.get(1));
		}
		if (limit != null && limit.size() == 2) {
			request.append(" LIMIT ").append(limit.get(0)).append(",").append(limit.get(1));
		}

		try {
			conn = getConnection();
			statement = conn.prepareStatement(request.toString());
			Log.log("Executing request '" + request + "'");

			this.insertStatementPreparedValues(statement, objects);
			result = DatabaseSet.resultSetToDatabaseSet(statement.executeQuery());
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			closeConnection(conn);
			closeStatement(statement);
		}

		return result;
	}

	/**
	 * Find a row data in the database
	 * @param table The table to start the selection
	 * @return The specific row selected in the specified table or null if not found.
	 */
	public DatabaseSet findFirst(String table) {
		return this.findFirst(table, null);
	}

	/**
	 * Find a row data in the database
	 * @param table The table to start the selection
	 * @param conditions A map which represents "WHERE" in SQL
	 * @return The specific row selected in the specified table or null if not found.
	 */
	public DatabaseSet findFirst(String table, Map<String, String> conditions) {
		return this.findFirst(table, conditions, null);
	}

	/**
	 * Find a row data in the database
	 * @param table The table to start the selection
	 * @param conditions A map which represents "WHERE" in SQL
	 * @param orderby A list which represents "ORDER BY" in SQL
	 * @return The specific row selected in the specified table or null if not found.
	 */
	public DatabaseSet findFirst(String table, Map<String, String> conditions, List<String> orderby) {
		return this.findFirst(table, conditions, orderby, null);
	}

	/**
	 * Find a row data in the database
	 * @param table The table to start the selection
	 * @param conditions A map which represents "WHERE" in SQL
	 * @param orderby A list which represents "ORDER BY" in SQL
	 * @param fields A list which includes all fields that will be returned by the request
	 * @return The specific row selected in the specified table or null if not found.
	 */
	public DatabaseSet findFirst(String table, Map<String, String> conditions, List<String> orderby, List<String> fields) {
		return this.findFirst(table, conditions, orderby, fields, null);
	}

	/**
	 * Find a row data in the database
	 * @param table The table to start the selection
	 * @param conditions A map which represents "WHERE" in SQL
	 * @param orderby A list which represents "ORDER BY" in SQL
	 * @param fields A list which includes all fields that will be returned by the request
	 * @param limit Used to precise a SQL limitation for the prepared statement
	 * @return The specific row selected in the specified table or null if not found.
	 */
	public DatabaseSet findFirst(String table, Map<String, String> conditions, List<String> orderby, List<String> fields, List<Integer> limit) {
		List<DatabaseSet> r = this.find(table, conditions, orderby, fields, limit);
		return (r != null && r.size() > 0) ? r.get(0) : null;
	}

	/**
	 * Save data into the database
	 * @param table Table where to save data
	 * @param fields Field & data to save
	 * @return True if the data was successfully saved.
	 */
	public boolean save(String table, Map<String, Object> fields) {
		return this.save(table, fields, null);
	}

	/**
	 * Save data into the database
	 * @param table Table where to save data
	 * @param fields Field & data to save
	 * @param conditions A conditions map used to do an update instead of an insert.
	 * @return True if the data was successfully saved.
	 */
	public boolean save(String table, Map<String, Object> fields, Map<String, String> conditions) {
		// Make request string
		StringBuilder request = new StringBuilder();
		List<Object> objects = new ArrayList<>();

		if (conditions == null) { // INSERT
			request.append("INSERT INTO `").append(table).append("` (");

			int keyCount = fields.keySet().size();
			int i = 1;
			int j = 1;

			// Keys
			for (String key : fields.keySet()) {
				request.append('`').append(key).append('`');
				request.append(i != keyCount ? ',' : ')');

				i++;
			}

			request.append(" VALUES (");
			// Values
			for (String key : fields.keySet()) {
				if (j != keyCount)
					request.append("?,");
				else
					request.append("?)");

				objects.add(fields.get(key));
				j++;
			}
		} else { // UPDATE
			request.append("UPDATE `").append(table).append("` SET ");

			// Keys & Values
			int keyCount = fields.keySet().size();
			int i = 1;

			for (String key : fields.keySet()) {
				Object o = fields.get(key);
				request.append('`').append(key).append("` = ?");
				request.append(i != keyCount ? ", " : "");

				objects.add(o);
				i++;
			}

			// Where clause
			this.prepareRequestWhere(request, conditions, objects);
		}

		return this.executeRequest(request, objects);
	}

	/**
	 * Process a custom request into the database.
	 * Used for very complex requests.
	 * @param request The request to execute
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
	 * Delete data from the database
	 * @param table Table where the data to remove is stored
	 * @param conditions Conditions map to filter data which have to be deleted
	 * @return True if the data was successfully deleted
	 */
	public boolean delete(String table, Map<String, String> conditions) {
		StringBuilder request = new StringBuilder();
		List<Object> objects = new ArrayList<>();

		request.append("DELETE FROM `").append(table).append('`');
		this.prepareRequestWhere(request, conditions, objects);

		return this.executeRequest(request, objects);
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
			Log.error("Mysql error: unable to connect to the database at " + this.host + ":" + this.port + ". Please retry.");

			Config.mysql = false;
			Config.enabled = true;
		}
	}

	/**
	 * Prepare the WHERE clause of a request and create a list of all objects to append.
	 * @param request Request string builder
	 * @param conditions Map with all conditions and values to handle
	 * @param objects An empty list where objects will be added
	 */
	private void prepareRequestWhere(StringBuilder request, Map<String, String> conditions, List<Object> objects) {
		if (conditions != null && conditions.size() > 0) {
			int count = conditions.size();
			int index = 1;

			request.append(" WHERE ");
			for (String k : conditions.keySet()) {
				String v = conditions.get(k);
				request.append('`').append(k).append("` = ?");
				request.append(index != count ? " AND " : "");

				objects.add(v);
				index++;
			}
		}
	}

	/**
	 * Insert a list of values inside a prepared statement
	 * @param statement Statement to be filled
	 * @param values Values to insert
	 * @throws SQLException if the index does not correspond to a parameter
	 * marker in the SQL statement; if a database access error occurs or
	 * this method is called on a closed <code>PreparedStatement</code>
	 */
	private void insertStatementPreparedValues(PreparedStatement statement, List<Object> values) throws SQLException {
		if (values != null) {
			int i = 1;
			for (Object o : values) {
				if (o instanceof String) {
					statement.setString(i, (String) o);
				} else if (o instanceof Integer) {
					statement.setInt(i, (Integer) o);
				} else if (o instanceof Long) {
					statement.setLong(i, (Long) o);
				} else if (o instanceof Float) {
					statement.setFloat(i, (Float) o);
				} else if (o instanceof Double) {
					statement.setDouble(i, (Double) o);
				} else if (o instanceof Timestamp) {
					statement.setTimestamp(i, (Timestamp) o);
				} else {
					statement.setNull(i, Types.TIMESTAMP);
				}
				i++;
			}
		}
	}

	/**
	 * Execute a request which DO NOT return data.
	 * @param request Request to execute
	 * @param objects Objects to link with the prepared request
	 * @return True if the the request has been processed, false otherwise.
	 */
	private boolean executeRequest(StringBuilder request, List<Object> objects) {
		Connection conn = null;
		PreparedStatement statement = null;

		try {
			conn = getConnection();
			if (conn != null) {
				statement = conn.prepareStatement(request.toString());
				this.insertStatementPreparedValues(statement, objects);

				Log.log("Executing request '" + request + "'");

				statement.execute();
				return true;
			} else {
				return false;
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		} finally {
			closeConnection(conn);
			closeStatement(statement);
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

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
	 * List of all tables created in the database
	 */
	private List<String> tables;

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
	 * Used to know if the object is connected to the database or not.
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
	 * Used to drop all data from a specific table passed in parameter
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
	 * Used to close the connection to the MySQL server
	 * @throws SQLException Exception throwed if the connection cannot be closed.
	 */
	public void close() throws SQLException {
		this.source.close();
	}

	/**
	 * Used to know if a table exists in the database or not.
	 * @param table The name of the table to search.
	 * @return True if the table exists.
	 */
	public boolean tableExists(String table) {
		if (!isConnected()) return false;

		if (this.tables == null)
			this.tables = getTables();

		return this.tables.contains(table);
	}

	/**
	 * Returns the version description of the connected MySQL server
	 * @return Version of the database server
	 */
	public String getMySQLVersion() {
		Connection conn = null;

		try {
			conn = getConnection();

			return (isConnected() ? conn.getMetaData().getDatabaseProductVersion() : "0.0");
		} catch (SQLException e) {
			e.printStackTrace();
			return "-1";
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

			assert conn != null;
			DatabaseMetaData dbm = conn.getMetaData();
			ResultSet result = dbm.getTables(null, null, "%", null);

			while (result.next()) {
				tables.add(result.getString(3));
			}
		} catch (Exception e) {
			e.printStackTrace();
			return tables;
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
		return find(table, conditions, orderby, fields, null, false);
	}

	/**
	 * Find data in the database
	 * @param table The table to start the selection
	 * @param conditions A map which represents "WHERE" in SQL
	 * @param orderby A list which represents "ORDER BY" in SQL
	 * @param fields A list which includes all fields that will be returned by the request
	 * @param limit Used to precise a SQL limitation for the prepared statement
	 * @param caseSensitive True if you want to do the selection with case-sensitive or not.
	 * @return List of all rows selected in the specified table.
	 */
	public List<DatabaseSet> find(String table, Map<String, String> conditions, List<String> orderby, List<String> fields, List<Integer> limit, boolean caseSensitive) {
		List<DatabaseSet> result = null;
		PreparedStatement sql = null;
		Connection conn = null;

		if (!isConnected()) return null;

		StringBuilder strFields = new StringBuilder("*");
		if (fields != null) {
			strFields = new StringBuilder();

			for (String field : fields)
				strFields.append(field).append(",");

			strFields = new StringBuilder(strFields.substring(0, strFields.length() - 1));
		}

		// Format fields & elements
		StringBuilder req = new StringBuilder("SELECT " + strFields + " FROM " + table + "");
		ArrayList<String> stringsToExec = new ArrayList<String>();
		if (conditions != null) {
			int count = conditions.size();
			int index = 1;

			req.append(" WHERE ");
			if (caseSensitive) req.append("BINARY ");

			for (String k : conditions.keySet()) {
				String v = conditions.get(k);

				if (index != count)
					req.append(k).append(" = ? AND ");
				else
					req.append(k).append(" = ?");

				stringsToExec.add(v);

				index++;
			}
		}

		if (orderby != null) req.append(" ORDER BY ").append(orderby.get(0)).append(" ").append(orderby.get(1));
		if (limit != null) req.append(" LIMIT ").append(limit.get(0)).append(",").append(limit.get(1));

		try {
			conn = getConnection();
			sql = conn.prepareStatement(req.toString());

			int i = 1;
			for (String s : stringsToExec) {
				sql.setString(i, s);
				i++;
			}

			result = DatabaseSet.resultSetToDatabaseSet(sql.executeQuery());
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			closeConnection(conn);
			closeStatement(sql);
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
		PreparedStatement sql = null;
		Connection conn = null;

		// Make request string
		StringBuilder req = new StringBuilder();
		List<Object> objsToExec = new ArrayList<>();
		if (conditions == null) { // INSERT
			req.append("INSERT INTO ").append(table).append(" (");

			int keyCount = fields.keySet().size();
			int i = 1;
			int j = 1;

			// Keys
			for (String key : fields.keySet()) {
				if (i != keyCount)
					req.append(key).append(",");
				else
					req.append(key).append(")");

				i++;
			}

			req.append(" VALUES (");
			// Values
			for (String key : fields.keySet()) {
				if (j != keyCount)
					req.append("?,");
				else
					req.append("?)");

				objsToExec.add(fields.get(key));
				j++;
			}
		} else { // UPDATE
			req.append("UPDATE ").append(table).append(" SET ");

			// Keys & Values
			int keyCount = fields.keySet().size();
			int i = 1;

			for (String key : fields.keySet()) {
				Object o = fields.get(key);

				if (i != keyCount)
					req.append(key).append("=?, ");
				else
					req.append(key).append("=?");

				objsToExec.add(o);
				i++;
			}

			// Conditions
			int CondsCount = conditions.size();
			int CondsIndex = 1;

			req.append(" WHERE ");
			for (String k : conditions.keySet()) {
				String v = conditions.get(k);

				if (CondsIndex != CondsCount)
					req.append(k).append(" = ? AND ");
				else
					req.append(k).append(" = ?");

				objsToExec.add(v);

				CondsIndex++;
			}
		}

		try {
			conn = getConnection();
			sql = conn.prepareStatement(req.toString());

			int i = 1;
			for (Object o : objsToExec) {
				if (o instanceof String)
					sql.setString(i, (String) o);
				else
					if (o instanceof Integer)
						sql.setInt(i, (Integer) o);
					else
						if (o instanceof Long)
							sql.setLong(i, (Long) o);
						else
							if (o instanceof Float)
								sql.setFloat(i, (Float) o);
							else
								if (o instanceof Double)
									sql.setDouble(i, (Double) o);
								else
									if (o instanceof Timestamp) {
										sql.setTimestamp(i, (Timestamp) o);
									} else {
										sql.setNull(i, Types.TIMESTAMP);
									}

				i++;
			}

			sql.execute();
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		} finally {
			closeConnection(conn);
			closeStatement(sql);
		}
	}

	/**
	 * Process a custom request into the database.
	 * Used for very complex requests.
	 * @param request The request to execute
	 */
	public void request(String request) {
		Statement s = null;
		Connection conn = null;

		try {
			conn = getConnection();
			s = conn.createStatement();

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
		PreparedStatement sql = null;
		Connection conn = null;

		// Make request string
		String req = "DELETE FROM " + table;
		List<Object> objsToExec = new ArrayList<>();

		// Conditions
		int CondsCount = conditions.size();
		int CondsIndex = 1;

		req += " WHERE ";
		for (String k : conditions.keySet()) {
			String v = conditions.get(k);

			if (CondsIndex != CondsCount)
				req += k + " = ? AND";
			else
				req += k + " = ?";

			objsToExec.add(v);

			CondsIndex++;
		}

		try {
			conn = getConnection();
			sql = conn.prepareStatement(req);

			int i = 1;
			for (Object o : objsToExec) {
				if (o instanceof String)
					sql.setString(i, (String) o);
				else
					if (o instanceof Integer)
						sql.setInt(i, (Integer) o);
					else
						if (o instanceof Float)
							sql.setFloat(i, (Float) o);
						else
							if (o instanceof Double)
								sql.setDouble(i, (Double) o);
							else
								if (o instanceof Timestamp)
									sql.setTimestamp(i, (Timestamp) o);

				i++;
			}

			sql.execute();
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		} finally {
			closeConnection(conn);
			closeStatement(sql);
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
		} catch (Exception e) {
			this.source = null;
			Log.error("Mysql error: unable to connect to the database at " + this.host + ":" + this.port + ". Please retry.");

			Config.mysql = false;
			Config.enabled = true;
		}
	}

	/**
	 * Returns the connection created by the source pool object.
	 * @return The MySQL Connection object.
	 */
	private Connection getConnection() {
		try {
			return (this.source != null) ? this.source.getConnection() : null;
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Method used to close a statement without to manage the exception each times.
	 * @param statement The statement to properly close
	 */
	private void closeStatement(Statement statement) {
		try {
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
			conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}

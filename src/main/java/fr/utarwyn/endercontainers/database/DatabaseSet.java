package fr.utarwyn.endercontainers.database;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class used to manage a database row more easily than the ResultSet class
 * @since 1.0.5
 * @author Utarwyn
 */
public class DatabaseSet {

	/**
	 * Object which stores all data collected for a row
	 * (String key -> Object value)
	 */
	private Map<String, Object> set;

	/**
	 * Construct a new database row object
	 */
	public DatabaseSet() {
		this.set = new HashMap<>();
	}

	/**
	 * Returns a String object for the row
	 * @param key The column used to get the value.
	 * @return The value stored in the selected column (key)
	 */
	public String getString(String key) {
		if (set.containsKey(key) && set.get(key) instanceof String)
			return (String) set.get(key);
		else
			return null;
	}

	/**
	 * Returns an Integer object for the row
	 * @param key The column used to get the value.
	 * @return The value stored in the selected column (key)
	 */
	public Integer getInteger(String key) {
		if (set.containsKey(key) && set.get(key) instanceof Integer)
			return (Integer) set.get(key);
		else
			return null;
	}

	/**
	 * Returns a Float object for the row
	 * @param key The column used to get the value.
	 * @return The value stored in the selected column (key)
	 */
	public Float getFloat(String key) {
		if (set.containsKey(key) && set.get(key) instanceof Float)
			return (Float) set.get(key);
		else
			return null;
	}

	/**
	 * Returns a Double object for the row
	 * @param key The column used to get the value.
	 * @return The value stored in the selected column (key)
	 */
	public Double getDouble(String key) {
		if (set.containsKey(key) && set.get(key) instanceof Double)
			return (Double) set.get(key);
		else
			return null;
	}

	/**
	 * Returns a Timestamp object for the row
	 * @param key The column used to get the value.
	 * @return The value stored in the selected column (key)
	 */
	public Timestamp getTimestamp(String key) {
		if (set.containsKey(key) && set.get(key) instanceof Timestamp)
			return (Timestamp) set.get(key);
		else
			return null;
	}

	/**
	 * Returns a Date object for the row
	 * @param key The column used to get the value.
	 * @return The value stored in the selected column (key)
	 */
	public Date getDate(String key) {
		if (set.containsKey(key) && set.get(key) instanceof Date)
			return (Date) set.get(key);
		else
			return null;
	}

	/**
	 * Returns all the data for the row
	 * @return Map with all data stored in memory for this row
	 */
	public Map<String, Object> getObjects() {
		return this.set;
	}

	/**
	 * Define a custom value for the current row.
	 * (It's a hack to simulate a database row)
	 * @param key The key where the value will be stored
	 * @param value The value to store
	 */
	public void setObject(String key, Object value) {
		set.put(key, value);
	}

	/**
	 * Transform a SQL ResultSet object into a DatabaseSet object
	 * @param resultSet Result from the SQL Connection object
	 * @return Converted data into database sets
	 */
	public static List<DatabaseSet> resultSetToDatabaseSet(ResultSet resultSet) {
		List<DatabaseSet> result = new ArrayList<>();

		try {
			int columns = resultSet.getMetaData().getColumnCount();

			while (resultSet.next()) {
				DatabaseSet set = new DatabaseSet();

				for (int i = 0; i < columns; i++)
					set.setObject(resultSet.getMetaData().getColumnName(i + 1), resultSet.getObject(i + 1));

				result.add(set);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				if (resultSet != null) resultSet.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		return result;
	}

	/**
	 * Create a conditions map easily with a list of conditions passed to the method.
	 * @param conditions The list of conditions to combine into a Java map
	 * @return The generated conditions map.
	 */
	public static Map<String, String> makeConditions(String... conditions) {
		Map<String, String> r = new HashMap<>();
		boolean key = true;
		String lastString = "";

		for (String s : conditions) {
			if (key) {
				lastString = s;
				key = false;
			} else {
				r.put(lastString, s);
				key = true;
			}
		}

		return r;
	}

	/**
	 * Create a fields map easily with a list of fields passed to the method.
	 * @param fields The list of fields to combine into a Java map
	 * @return The generated fields map.
	 */
	public static Map<String, Object> makeFields(Object... fields) {
		Map<String, Object> r = new HashMap<>();
		boolean key = true;
		Object lastString = "";

		for (Object s : fields) {
			if (key) {
				lastString = s;
				key = false;
			} else {
				r.put((String) lastString, s);
				key = true;
			}
		}

		return r;
	}

	/**
	 * Create a list of "order by" for a request easily
	 * @param column The column in which the order by will be applied
	 * @param type The type of ordering
	 * @return The generated orders list
	 */
	public static List<String> makeOrderBy(String column, String type) {
		return new ArrayList<String>() {{
			add(column); add(type);
		}};
	}

	/**
	 * Create a list of "limit" for a request easily
	 * @param begin The begin number of the limitation
	 * @param num The number used in the limit action
	 * @return The generated limits list
	 */
	public static List<Integer> makeLimit(Integer begin, Integer num) {
		return new ArrayList<Integer>() {{
			add(begin); add(num);
		}};
	}

}

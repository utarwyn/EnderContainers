package fr.utarwyn.endercontainers.database;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Class used to manage a database row more easily than the ResultSet class
 *
 * @author Utarwyn
 * @since 1.0.5
 */
public class DatabaseSet {

    /**
     * Object which stores all data collected for a row
     * (String key -> Object value)
     */
    private ConcurrentHashMap<String, Object> set;

    /**
     * Construct a new database row object
     */
    public DatabaseSet() {
        this.set = new ConcurrentHashMap<>();
    }

    /**
     * Transform a SQL ResultSet object into a DatabaseSet object
     *
     * @param resultSet Result from the SQL Connection object
     * @return Converted data into database sets
     */
    static List<DatabaseSet> resultSetToDatabaseSet(ResultSet resultSet) throws SQLException {
        List<DatabaseSet> result = new ArrayList<>();
        int columns = resultSet.getMetaData().getColumnCount();

        while (resultSet.next()) {
            DatabaseSet set = new DatabaseSet();

            for (int i = 0; i < columns; i++) {
                set.setObject(resultSet.getMetaData().getColumnName(i + 1), resultSet.getObject(i + 1));
            }

            result.add(set);
        }

        return result;
    }

    /**
     * Returns a String object for the row
     *
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
     *
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
     * Returns a Timestamp object for the row
     *
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
     * Returns all keys for the row
     *
     * @return Map with all keys for this row
     */
    public List<String> getKeys() {
        return new ArrayList<>(this.set.keySet());
    }

    /**
     * Returns all values for the row
     *
     * @return Map with all values for this row
     */
    public List<Object> getValues() {
        return new ArrayList<>(this.set.values());
    }

    /**
     * Define a custom value for the current row.
     * (It's a hack to simulate a database row)
     *
     * @param key   The key where the value will be stored
     * @param value The value to store
     */
    public void setObject(String key, Object value) {
        set.put(key, value);
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder("{DatabaseSet #" + this.hashCode() + " (");

        int i = 0;
        for (ConcurrentHashMap.Entry<String, Object> entry : this.set.entrySet()) {
            s.append(entry.getKey()).append("=").append(entry.getValue());
            s.append((i < this.set.size() - 1) ? " " : "");

            i++;
        }

        s.append(")}");

        return s.toString();
    }

}

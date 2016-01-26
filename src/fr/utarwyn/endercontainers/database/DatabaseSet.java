package fr.utarwyn.endercontainers.database;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DatabaseSet {

    private Map<String, Object> set = new HashMap<String, Object>();

    public void setObject(String key, Object value) {
        set.put(key, value);
    }

    public String getString(String key) {
        if (set.containsKey(key) && set.get(key) instanceof String)
            return (String) set.get(key);
        else
            return null;
    }

    public Integer getInteger(String key) {
        if (set.containsKey(key) && set.get(key) instanceof Integer)
            return (Integer) set.get(key);
        else
            return null;
    }

    public Float getFloat(String key) {
        if (set.containsKey(key) && set.get(key) instanceof Float)
            return (Float) set.get(key);
        else
            return null;
    }

    public Double getDouble(String key) {
        if (set.containsKey(key) && set.get(key) instanceof Double)
            return (Double) set.get(key);
        else
            return null;
    }

    public Timestamp getTimestamp(String key) {
        if (set.containsKey(key) && set.get(key) instanceof Timestamp)
            return (Timestamp) set.get(key);
        else
            return null;
    }

    public Date getDate(String key) {
        if (set.containsKey(key) && set.get(key) instanceof Date)
            return (Date) set.get(key);
        else
            return null;
    }


    public static List<DatabaseSet> resultSetToDatabaseSet(ResultSet resultSet) {
        List<DatabaseSet> result = new ArrayList<DatabaseSet>();

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

    public static Map<String, String> makeConditions(String... conditions) {
        Map<String, String> r = new HashMap<String, String>();
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

    public static Map<String, Object> makeFields(Object... conditions) {
        Map<String, Object> r = new HashMap<String, Object>();
        boolean key = true;
        Object lastString = "";

        for (Object s : conditions) {
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

    public static List<String> makeOrderBy(String column, String type) {
        List<String> r = new ArrayList<String>();

        r.add(column);
        r.add(type);

        return r;
    }

    public static List<Integer> makeLimit(Integer begin, Integer num) {
        List<Integer> r = new ArrayList<Integer>();

        r.add(begin);
        r.add(num);

        return r;
    }
}

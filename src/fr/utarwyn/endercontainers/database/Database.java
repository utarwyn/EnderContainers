package fr.utarwyn.endercontainers.database;

import fr.utarwyn.endercontainers.utils.Config;
import fr.utarwyn.endercontainers.utils.CoreUtils;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Database {

    private static String host = Config.DB_HOST;
    private static Integer port = Config.DB_PORT;
    private static String user = Config.DB_USER;
    private static String pass = Config.DB_PASS;
    private static String DB = Config.DB_BDD;

    private static Connection conn;
    private static String lastRequest;


    public void setBDD(String BDD) {
        Database.DB = BDD;
    }

    public static Connection getConnection() {
        return Database.conn;
    }

    public void connect() {
        try {
            if (Database.conn == null || Database.conn.isClosed()) {
                Database.conn = DriverManager.getConnection("jdbc:mysql://" + Database.host + ":" + Database.port + "/" + Database.DB, user, pass);
            }
        } catch (SQLException e) {
            CoreUtils.error("Mysql error: " + e.getMessage());
        }
    }

    private void disconnect() {
        try {
            if (getConnection() != null && !getConnection().isClosed()) {
                Database.conn.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public void createDatabase(String dbName) {
        Statement s;
        try {
            Connection conn = DriverManager.getConnection("jdbc:mysql://" + Database.host + ":" + Database.port + "/?user=" + Database.user + "&password=" + Database.pass);
            s = conn.createStatement();
            s.executeUpdate("CREATE DATABASE " + dbName);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public List<DatabaseSet> find(String table) {
        return this.find(table, null);
    }

    public List<DatabaseSet> find(String table, Map<String, String> conditions) {
        return this.find(table, conditions, null);
    }

    public List<DatabaseSet> find(String table, Map<String, String> conditions, List<String> orderby) {
        return this.find(table, conditions, orderby, null);
    }

    public List<DatabaseSet> find(String table, Map<String, String> conditions, List<String> orderby, List<Integer> limit) {
        connect();

        List<DatabaseSet> result = null;
        PreparedStatement sql = null;
        if (getConnection() == null) return null;

        // Format fields & elements
        String req = "SELECT * FROM " + table + "";
        ArrayList<String> stringsToExec = new ArrayList<>();
        if (conditions != null) {
            int count = conditions.size();
            int index = 1;

            req += " WHERE ";
            for (String k : conditions.keySet()) {
                String v = conditions.get(k);

                if (index != count)
                    req += k + " = ? AND";
                else
                    req += k + " = ?";

                stringsToExec.add(v);

                index++;
            }
        }

        if (orderby != null) req += " ORDER BY " + orderby.get(0) + " " + orderby.get(1);
        if (limit != null) req += " LIMIT " + limit.get(0) + "," + limit.get(1);

        try {
            lastRequest = req;
            sql = getConnection().prepareStatement(req);

            int i = 1;
            for (String s : stringsToExec) {
                sql.setString(i, s);
                i++;
            }

            result = DatabaseSet.resultSetToDatabaseSet(sql.executeQuery());
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (sql != null) sql.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            disconnect();
        }

        return result;
    }

    public DatabaseSet findFirst(String table) {
        List<DatabaseSet> r = this.find(table);
        if (r == null) return null;
        else return r.get(0);
    }

    public DatabaseSet findFirst(String table, Map<String, String> conditions) {
        List<DatabaseSet> r = this.find(table, conditions);
        if (r == null) return null;
        else return r.get(0);
    }

    public DatabaseSet findFirst(String table, Map<String, String> conditions, List<String> orderby) {
        List<DatabaseSet> r = this.find(table, conditions, orderby);
        if (r == null) return null;
        else return r.get(0);
    }

    public DatabaseSet findFirst(String table, Map<String, String> conditions, List<String> orderby, List<Integer> limit) {
        List<DatabaseSet> r = this.find(table, conditions, orderby, limit);
        if (r == null) return null;
        else return r.get(0);
    }


    public boolean save(String table, Map<String, Object> fields) {
        return this.save(table, fields, null);
    }

    public boolean save(String table, Map<String, Object> fields, Map<String, String> conditions) {
        connect();
        PreparedStatement sql = null;

        // Make request string
        String req = "";
        List<Object> objsToExec = new ArrayList<Object>();
        if (conditions == null) { // INSERT
            req += "INSERT INTO " + table + " (";

            int keyCount = fields.keySet().size();
            int i = 1;
            int j = 1;

            // Keys
            for (String key : fields.keySet()) {
                if (i != keyCount)
                    req += key + ",";
                else
                    req += key + ")";

                i++;
            }

            req += " VALUES (";
            // Values
            for (String key : fields.keySet()) {
                if (j != keyCount)
                    req += "?,";
                else
                    req += "?)";

                objsToExec.add(fields.get(key));
                j++;
            }
        } else { // UPDATE
            req += "UPDATE " + table + " SET ";

            // Keys & Values
            int keyCount = fields.keySet().size();
            int i = 1;

            for (String key : fields.keySet()) {
                Object o = fields.get(key);

                if (i != keyCount)
                    req += key + "=?, ";
                else
                    req += key + "=?";

                objsToExec.add(o);
                i++;
            }

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
        }

        try {
            lastRequest = req;
            sql = getConnection().prepareStatement(req);

            int i = 1;
            for (Object o : objsToExec) {
                if (o instanceof String)
                    sql.setString(i, (String) o);
                else if (o instanceof Integer)
                    sql.setInt(i, (Integer) o);
                else if (o instanceof Float)
                    sql.setFloat(i, (Float) o);
                else if (o instanceof Double)
                    sql.setDouble(i, (Double) o);
                else if (o instanceof Timestamp)
                    sql.setTimestamp(i, (Timestamp) o);

                i++;
            }

            sql.execute();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (sql != null) sql.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            disconnect();
        }
    }


    public boolean delete(String table, Map<String, String> conditions) {
        connect();

        PreparedStatement sql = null;

        // Make request string
        String req = "DELETE FROM " + table;
        List<Object> objsToExec = new ArrayList<Object>();

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
            lastRequest = req;
            sql = getConnection().prepareStatement(req);

            int i = 1;
            for (Object o : objsToExec) {
                if (o instanceof String)
                    sql.setString(i, (String) o);
                else if (o instanceof Integer)
                    sql.setInt(i, (Integer) o);
                else if (o instanceof Float)
                    sql.setFloat(i, (Float) o);
                else if (o instanceof Double)
                    sql.setDouble(i, (Double) o);
                else if (o instanceof Timestamp)
                    sql.setTimestamp(i, (Timestamp) o);

                i++;
            }

            sql.execute();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (sql != null) sql.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            disconnect();
        }
    }


    public String getLastRequest() {
        return lastRequest;
    }
}

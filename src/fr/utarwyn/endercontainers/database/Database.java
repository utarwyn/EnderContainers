package fr.utarwyn.endercontainers.database;

import fr.utarwyn.endercontainers.utils.Config;
import fr.utarwyn.endercontainers.utils.CoreUtils;
import org.apache.commons.dbcp2.BasicDataSource;

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

    public static BasicDataSource source;
    private static String lastRequest;
    private static List<String> tables;

    public Database(){
        this.createPool();
    }



    private void createPool() {
        try {
            source = new BasicDataSource();
            source.setDriverClassName("com.mysql.jdbc.Driver");
            source.setUrl("jdbc:mysql://" + Database.host + ":" + Database.port + "/" + Database.DB);
            source.setUsername(Database.user);
            source.setPassword(Database.pass);

            source.setInitialSize(1);
            source.setMaxOpenPreparedStatements(8);
            source.setMaxTotal(8);

            // Connection test
            Connection conn = source.getConnection();
            conn.close();

            CoreUtils.log(Config.pluginPrefix + "§aMysql: connected to the database '" + Database.DB + "'.");
        } catch (Exception e) {
            Database.source = null;
            CoreUtils.error("Mysql error: unable to connect to the database at §c" + Database.host + ":" + Database.port + "§4. Please retry.");
            CoreUtils.log(Config.pluginPrefix + "§4Module §6Mysql §4disabled.", true);

            Config.mysql = false;Config.enabled = true;
        }
    }

    public static Connection getConnection() {
        try {
            return (Database.source != null) ? Database.source.getConnection() : null;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
    public static Boolean isConnected(){
        try{
            return (source != null && !source.isClosed());
        } catch(Exception e){
            return false;
        }
    }


    public void createDatabase(String dbName) {
        Statement s     = null;
        Connection conn = null;
        try {
            conn = getConnection();
            s    = conn.createStatement();

            s.executeUpdate("CREATE DATABASE " + dbName);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeStatement(s);
            closeConnection(conn);
        }
    }
    public void emptyTable(String tableName) {
        Statement s     = null;
        Connection conn = null;

        try {
            conn = getConnection();
            s    = conn.createStatement();

            s.executeUpdate("USE " + Database.DB);
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
    public Boolean tableExists(String table){
        if(!isConnected()) return false;

        if(tables == null) tables = getTables();
        return tables.contains(table);
    }

    public String getMySQLVersion(){
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
    public List<String> getTables(){
        Connection conn     = null;
        List<String> tables = new ArrayList<>();

        try{
            conn = getConnection();

            assert conn != null;
            DatabaseMetaData dbm = conn.getMetaData();
            ResultSet result     = dbm.getTables(null, null, "%", null);

            while(result.next()){
                tables.add(result.getString(3));
            }
        } catch(Exception e){
            e.printStackTrace();
            return tables;
        } finally {
            closeConnection(conn);
        }

        return tables;
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

    public List<DatabaseSet> find(String table, Map<String, String> conditions, List<String> orderby, List<String> fields) {
        return find(table, conditions, orderby, fields, null);
    }

    public List<DatabaseSet> find(String table, Map<String, String> conditions, List<String> orderby, List<String> fields, List<Integer> limit) {
        return find(table, conditions, orderby, fields, null, false);
    }

    public List<DatabaseSet> find(String table, Map<String, String> conditions, List<String> orderby, List<String> fields, List<Integer> limit, boolean caseSensitive) {
        List<DatabaseSet> result = null;
        PreparedStatement sql = null;
        Connection conn       = null;

        if (!isConnected()) return null;

        String strFields = "*";
        if(fields != null){
            strFields = "";

            for(String field : fields)
                strFields += field + ",";

            strFields = strFields.substring(0, strFields.length() - 1);
        }

        // Format fields & elements
        String req = "SELECT " + strFields + " FROM " + table + "";
        ArrayList<String> stringsToExec = new ArrayList<>();
        if (conditions != null) {
            int count = conditions.size();
            int index = 1;

            req += " WHERE ";
            if(caseSensitive) req += "BINARY ";

            for (String k : conditions.keySet()) {
                String v = conditions.get(k);

                if (index != count)
                    req += k + " = ? AND ";
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
            conn        = getConnection();
            sql         = conn.prepareStatement(req);

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

    public DatabaseSet findFirst(String table, Map<String, String> conditions, List<String> orderby, List<String> fields) {
        List<DatabaseSet> r = this.find(table, conditions, orderby, fields);
        if (r == null) return null;
        else return r.get(0);
    }


    public DatabaseSet findFirst(String table, Map<String, String> conditions, List<String> orderby, List<String> fields, List<Integer> limit) {
        List<DatabaseSet> r = this.find(table, conditions, orderby, fields, limit);
        if (r == null) return null;
        else return r.get(0);
    }


    public boolean save(String table, Map<String, Object> fields) {
        return this.save(table, fields, null);
    }

    public boolean save(String table, Map<String, Object> fields, Map<String, String> conditions) {
        PreparedStatement sql = null;
        Connection conn       = null;

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
                    req += k + " = ? AND ";
                else
                    req += k + " = ?";

                objsToExec.add(v);

                CondsIndex++;
            }
        }

        try {
            lastRequest = req;
            conn        = getConnection();
            sql = conn.prepareStatement(req);

            int i = 1;
            for (Object o : objsToExec) {
                if (o instanceof String)
                    sql.setString(i, (String) o);
                else if (o instanceof Integer)
                    sql.setInt(i, (Integer) o);
                else if (o instanceof Long)
                    sql.setLong(i, (Long) o);
                else if (o instanceof Float)
                    sql.setFloat(i, (Float) o);
                else if (o instanceof Double)
                    sql.setDouble(i, (Double) o);
                else if (o instanceof Timestamp) {
                    sql.setTimestamp(i, (Timestamp) o);
                }else{
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

    public void request(String request){
        Statement s     = null;
        Connection conn = null;

        try {
            conn = getConnection();
            s    = conn.createStatement();

            s.executeUpdate(request);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeStatement(s);
            closeConnection(conn);
        }
    }

    public boolean delete(String table, Map<String, String> conditions) {
        PreparedStatement sql = null;
        Connection conn       = null;

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
            conn        = getConnection();
            sql         = conn.prepareStatement(req);

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
            closeConnection(conn);
            closeStatement(sql);
        }
    }


    public String getLastRequest() {
        return lastRequest;
    }


    private void closeStatement(Statement statement){
        try {
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    private void closeConnection(Connection conn){
        try {
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

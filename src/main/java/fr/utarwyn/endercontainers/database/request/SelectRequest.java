package fr.utarwyn.endercontainers.database.request;

import fr.utarwyn.endercontainers.database.Database;
import fr.utarwyn.endercontainers.database.DatabaseManager;
import fr.utarwyn.endercontainers.database.DatabaseSet;
import org.apache.commons.lang.StringUtils;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * A select request
 *
 * @author Utarwyn <maxime.malgorn@laposte.net>
 * @since 2.2.0
 */
public class SelectRequest implements IRequest {

    private Database database;

    private String[] fields;

    private String[] froms;

    private String[] conditions;

    private String[] orders;

    private String[] groupsBy;

    private int[] limits;

    private Object[] attributes;

    private List<String[]> joins;

    private List<String[]> leftJoins;

    public SelectRequest(Database db, String... fields) {
        this.database = db;
        this.fields = DatabaseManager.escapeFieldArray(fields);
        this.conditions = new String[0];
        this.orders = new String[0];
        this.groupsBy = new String[0];
        this.attributes = new String[0];
        this.limits = new int[0];

        this.joins = new ArrayList<>();
        this.leftJoins = new ArrayList<>();

        if (this.fields.length == 0) {
            this.fields = new String[]{"*"};
        }
    }

    public Object[] getAttributes() {
        return this.attributes;
    }

    public SelectRequest from(String... froms) {
        this.froms = DatabaseManager.escapeFieldArray(froms);
        return this;
    }

    public SelectRequest join(String table, String field1, String field2) {
        this.joins.add(DatabaseManager.escapeFieldArray(new String[]{table, field1, field2}));
        return this;
    }

    public SelectRequest leftjoin(String table, String field1, String field2) {
        this.leftJoins.add(DatabaseManager.escapeFieldArray(new String[]{table, field1, field2}));
        return this;
    }

    public SelectRequest where(String... conditions) {
        this.conditions = conditions;
        return this;
    }

    public SelectRequest groupBy(String... groupsBy) {
        this.groupsBy = groupsBy;
        return this;
    }

    public SelectRequest order(String... orders) {
        this.orders = orders;
        return this;
    }

    public SelectRequest limit(int length) {
        this.limits = new int[]{length, -1};
        return this;
    }

    public SelectRequest limit(int begin, int end) {
        this.limits = new int[]{begin, end};
        return this;
    }

    public SelectRequest attributes(Object... attributes) {
        this.attributes = attributes;
        return this;
    }

    public DatabaseSet find() throws SQLException {
        // We set the results limit at 1 to optimize the request
        this.limit(1);

        List<DatabaseSet> sets = this.findAll();
        return (sets == null || sets.isEmpty()) ? null : sets.get(0);
    }

    public List<DatabaseSet> findAll() throws SQLException {
        return this.database.execQueryStatement(this);
    }

    @Override
    public String getRequest() {
        StringBuilder request = new StringBuilder("SELECT ");

        request.append(StringUtils.join(this.fields, ","));
        request.append(" FROM ");
        request.append(StringUtils.join(this.froms, ","));

        for (String[] join : this.joins) {
            request.append(" JOIN ").append(join[0]).append(" ON ").append(join[1]).append(" = ").append(join[2]);
        }

        for (String[] join : this.leftJoins) {
            request.append(" LEFT JOIN ").append(join[0]).append(" ON ").append(join[1]).append(" = ").append(join[2]);
        }

        if (this.conditions.length > 0) {
            request.append(" WHERE ").append(StringUtils.join(this.conditions, " AND "));
        }
        if (this.groupsBy.length > 0) {
            request.append(" GROUP BY ").append(StringUtils.join(this.groupsBy, ","));
        }
        if (this.orders.length > 0) {
            request.append(" ORDER BY ").append(StringUtils.join(this.orders, ","));
        }
        if (this.limits.length > 0) {
            if (this.limits[1] > 0) { // limites
                request.append(" LIMIT ").append(this.limits[0]).append(",").append(this.limits[1]);
            } else {                  // nombre à récupérer
                request.append(" LIMIT ").append(this.limits[0]);
            }
        }

        return request.toString();
    }

}

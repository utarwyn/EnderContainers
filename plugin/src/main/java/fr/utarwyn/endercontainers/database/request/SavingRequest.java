package fr.utarwyn.endercontainers.database.request;

import fr.utarwyn.endercontainers.database.Database;
import fr.utarwyn.endercontainers.database.DatabaseManager;
import org.apache.commons.lang.StringUtils;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * A saving request.
 * Can execute an INSERT or an UPDATE request on the database.
 *
 * @author Utarwyn <maxime.malgorn@laposte.net>
 * @since 2.2.0
 */
public class SavingRequest implements Request {

    /**
     * Used database
     */
    private Database database;

    /**
     * Columns to update with the request
     */
    private String[] fields;

    /**
     * Table to update
     */
    private String table;

    /**
     * Conditions of the request. Could be empty when inserting values.
     */
    private String[] conditions;

    /**
     * Values to insert in the database
     */
    private Object[] values;

    /**
     * List of securized SQL attributes
     */
    private List<Object> attributes;

    /**
     * Could be set to true to perform a REPLACE request.
     */
    private boolean replaceIfExists;

    /**
     * Construct the request with basic informations.
     *
     * @param database database which contains the table to update
     * @param table    table to update
     */
    public SavingRequest(Database database, String table) {
        this.database = database;
        this.table = table;

        this.fields = new String[0];
        this.conditions = new String[0];
        this.values = new Object[0];
        this.attributes = new ArrayList<>();
    }

    public Object[] getAttributes() {
        return this.attributes.toArray();
    }

    public SavingRequest fields(String... fields) {
        this.fields = DatabaseManager.espaceFields(fields);
        return this;
    }

    public SavingRequest values(Object... values) {
        this.values = values;
        this.attributes(values);

        return this;
    }

    public SavingRequest where(String... conditions) {
        this.conditions = conditions;
        return this;
    }

    public SavingRequest attributes(Object... attributes) {
        Collections.addAll(this.attributes, attributes);
        return this;
    }

    public SavingRequest replaceIfExists() {
        this.replaceIfExists = true;
        return this;
    }

    public boolean execute() throws SQLException {
        return this.database.execUpdateStatement(this);
    }

    @Override
    public String getRequest() {
        StringBuilder request = new StringBuilder();

        // We do some verifications on object's attributes
        if (this.table == null) {
            throw new NullPointerException("Table seems to be null");
        }
        if (this.fields.length == 0 || this.values.length == 0) {
            throw new IllegalArgumentException("You must add at least one field and one value");
        }
        if (this.fields.length != this.values.length) {
            throw new IllegalArgumentException("Number of fields and values seems to be different");
        }

        // Now we just have to create a beautiful request
        if (this.conditions.length > 0) {
            request.append("UPDATE `").append(this.table).append('`');

            request.append(" SET");
            for (String field : this.fields) {
                request.append(" ").append(field).append(" = ?,");
            }
            request.deleteCharAt(request.length() - 1);

            request.append(" WHERE ").append(StringUtils.join(this.conditions, " AND "));
        } else {
            String action = this.replaceIfExists ? "REPLACE" : "INSERT";

            request.append(action).append(" INTO `").append(this.table).append('`');
            request.append("(").append(StringUtils.join(this.fields, ",")).append(")");

            request.append(" VALUES ");
            request.append("(").append(this.generateFakeParameters(this.values)).append(")");
        }

        return request.toString();
    }

    private String generateFakeParameters(Object[] values) {
        return StringUtils.join(Arrays.stream(values).map(v -> '?').toArray(), ",");
    }

}

package fr.utarwyn.endercontainers.database.request;

import fr.utarwyn.endercontainers.database.Database;
import org.apache.commons.lang.StringUtils;

import java.sql.SQLException;

/**
 * Builds a delete request to perform in the database.
 *
 * @author Utarwyn <maxime.malgorn@laposte.net>
 * @since 2.2.0
 */
public class DeleteRequest implements Request {

    /**
     * Database object
     */
    private final Database database;

    /**
     * Conditions to apply for deleting rows
     */
    private final String[] conditions;

    /**
     * Table where the request has to be performed
     */
    private String table;

    /**
     * Attributs for conditions
     */
    private Object[] attributes;

    /**
     * Construct a delete request builder.
     *
     * @param database   database object
     * @param conditions conditions to apply for deleting rows
     */
    public DeleteRequest(Database database, String... conditions) {
        this.database = database;
        this.conditions = conditions;
        this.attributes = new Object[0];
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object[] getAttributes() {
        return this.attributes;
    }

    public DeleteRequest from(String table) {
        this.table = table;
        return this;
    }

    public DeleteRequest attributes(Object... attributes) {
        this.attributes = attributes;
        return this;
    }

    public boolean execute() throws SQLException {
        return this.database.execUpdateStatement(this);
    }

    @Override
    public String getRequest() {
        StringBuilder sb = new StringBuilder();

        // Table does not have to be null at this point!
        if (this.table == null) {
            throw new NullPointerException("Table seems to be null");
        }

        sb.append("DELETE FROM `");
        sb.append(this.table);
        sb.append("`");

        // Construct conditions if exists
        if (this.conditions.length > 0) {
            sb.append(" WHERE ");
            sb.append(StringUtils.join(this.conditions, " AND "));
        }

        return sb.toString();
    }

}

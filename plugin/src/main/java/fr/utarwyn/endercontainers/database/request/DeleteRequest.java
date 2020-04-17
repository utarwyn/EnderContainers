package fr.utarwyn.endercontainers.database.request;

import fr.utarwyn.endercontainers.database.Database;
import org.apache.commons.lang.StringUtils;

import java.sql.SQLException;

/**
 * A delete request
 *
 * @author Utarwyn <maxime.malgorn@laposte.net>
 * @since 2.2.0
 */
public class DeleteRequest implements Request {

    private Database database;

    private String table;

    private String[] conditions;

    private Object[] attributes;

    public DeleteRequest(Database database, String... conditions) {
        this.database = database;
        this.conditions = conditions;
        this.attributes = new Object[0];
    }

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
        // Table does not have to be null at this point!
        if (this.table == null) {
            throw new NullPointerException("Table seems to be null");
        }
        if (this.conditions.length == 0) {
            throw new IllegalArgumentException("You must use at least one condition");
        }

        // Create the request string
        return "DELETE FROM `" + this.table + "` WHERE " +
                StringUtils.join(this.conditions, " AND ");
    }

}

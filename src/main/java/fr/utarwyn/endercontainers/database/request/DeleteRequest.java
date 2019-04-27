package fr.utarwyn.endercontainers.database.request;

import fr.utarwyn.endercontainers.database.Database;
import org.apache.commons.lang.StringUtils;

import java.sql.SQLException;

/**
 * A delete request
 * @since 2.2.0
 * @author Utarwyn <maxime.malgorn@laposte.net>
 */
public class DeleteRequest implements IRequest {

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

	public boolean execute() {
		try {
			return this.database.execUpdateStatement(this);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public String getRequest() {
		// Table does not have to be null at this point!
		if (this.table == null) {
			throw new NullPointerException("From table seems to be null!");
		}

		// Create the request string
		return "DELETE FROM `" + this.table + "` WHERE " +
				StringUtils.join(this.conditions, " AND ");
	}

}

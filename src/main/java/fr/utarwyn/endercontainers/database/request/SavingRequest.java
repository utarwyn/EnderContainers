package fr.utarwyn.endercontainers.database.request;

import fr.utarwyn.endercontainers.database.Database;
import fr.utarwyn.endercontainers.database.DatabaseManager;
import org.apache.commons.lang.StringUtils;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A saving request
 * @since 2.2.0
 * @author Utarwyn <maxime.malgorn@laposte.net>
 */
public class SavingRequest implements IRequest {

	private Database database;

	private String[] fields;

	private String table;

	private String[] conditions;

	private Object[] values;

	private List<Object> attributes;

	private boolean replaceIfExists;

	public SavingRequest(Database database, String table) {
		this.database = database;
		this.table = table;

		this.fields = new String[0];
		this.conditions = new String[0];
		this.attributes = new ArrayList<>();
	}

	public Object[] getAttributes() {
		return this.attributes.toArray();
	}

	public SavingRequest fields(String... fields) {
		this.fields = DatabaseManager.escapeFieldArray(fields);
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
		if (this.table == null)
			throw new NullPointerException("Table seems to be null!");
		if (this.fields.length == 0 && this.conditions.length > 0)
			throw new IllegalArgumentException("Bad fields or conditions number");
		if (this.fields.length != this.values.length && (this.conditions.length > 0 || this.fields.length > 0))
			throw new IllegalArgumentException("Number of fields and values seems to be different");

		// Now we just have to create a beautiful request
		if (this.conditions.length > 0) {
			request.append("UPDATE `").append(this.table).append('`');

			request.append(" SET");
			for (String field : this.fields) {
				request.append(" ").append(field).append(" = ?,");
			}

			if (this.fields.length > 0) {
				request.deleteCharAt(request.length() - 1);
			}

			request.append(" WHERE ").append(StringUtils.join(this.conditions, " AND "));
		} else {
			String action = "INSERT";

			if (this.replaceIfExists)
				action = "REPLACE";

			request.append(action).append(" INTO `").append(this.table).append('`');

			if (this.fields.length > 0) {
				request.append("(").append(StringUtils.join(this.fields, ",")).append(")");
			}

			request.append(" VALUES ");
			request.append("(").append(this.generateAttrValues(this.values)).append(")");
		}


		return request.toString();
	}

	private String generateAttrValues(Object[] values) {
		String[] tab = new String[values.length];

		for (int i = 0; i < tab.length; i++) tab[i] = "?";

		return StringUtils.join(tab, ",");
	}

}

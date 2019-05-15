package fr.utarwyn.endercontainers.migration.migration2_0_1;

import fr.utarwyn.endercontainers.configuration.Files;
import fr.utarwyn.endercontainers.database.Database;
import fr.utarwyn.endercontainers.database.DatabaseSet;
import fr.utarwyn.endercontainers.util.ItemSerializer;

import java.sql.SQLException;
import java.util.List;

public class MigrationMySQL2_0_1 extends Migration2_0_1 {

	@Override
	void reconfigureChestsContent() throws SQLException {
		Database database = getDatabase();
		if (database == null) return;

		String chestsTable = Files.getConfiguration().getMysqlTablePrefix() + "enderchests";
		List<DatabaseSet> chests = database.select().from(chestsTable).findAll();

		String contents;

		for (DatabaseSet chest : chests) {
			contents = chest.getString("contents");

			if (!isBase64Encoded(contents)) {
				database.update(chestsTable).fields("contents")
						.values(ItemSerializer.base64Serialization(ItemSerializer.experimentalDeserialization(contents)))
						.fields("`id` = ?").attributes(chest.getInteger("id"))
						.execute();
			}
		}
	}

}

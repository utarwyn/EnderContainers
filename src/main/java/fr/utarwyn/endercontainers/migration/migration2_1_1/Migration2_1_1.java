package fr.utarwyn.endercontainers.migration.migration2_1_1;

import fr.utarwyn.endercontainers.migration.Migration;
import fr.utarwyn.endercontainers.util.Log;

public class Migration2_1_1 extends Migration {

	public Migration2_1_1() {
		super("2.1.0", "2.1.1");
	}

	@Override
	public void perform() {
		/* ------------------- */
		/*  Update locales...  */
		/* ------------------- */
		Log.log("Update locales...", true);
		this.updateLocales();
	}

	private void updateLocales() {
		// TODO: I need to add to all locale files new keys next/previous page for menus
	}

}

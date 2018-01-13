package fr.utarwyn.endercontainers.migration.migration2_0_1;

import fr.utarwyn.endercontainers.migration.Migration;
import fr.utarwyn.endercontainers.util.Log;

import java.util.regex.Pattern;

public abstract class Migration2_0_1 extends Migration {

	private static Pattern BASE64_PATTERN = Pattern.compile("([A-Za-z0-9+/]{4})*([A-Za-z0-9+/]{4}|[A-Za-z0-9+/]{3}=|[A-Za-z0-9+/]{2}==)");

	Migration2_0_1() {
		super("2.0.0", "2.0.1");
	}

	@Override
	public void perform() {
		/* ------------------------- */
		/*  Update configuration...  */
		/* ------------------------- */
		Log.log("Update old configuration...", true);
		if (!this.updateConfiguration()) return;

		/* ---------------------------- */
		/*  Reconfigure chests data...  */
		/* ---------------------------- */
		Log.log("Reconfigure chests content...", true);
		this.reconfigureChestsContent();
	}

	abstract void reconfigureChestsContent();

	boolean isBase64Encoded(String data) {
		return BASE64_PATTERN.matcher(data).matches();
	}

}

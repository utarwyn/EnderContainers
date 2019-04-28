package fr.utarwyn.endercontainers.migration.migration2_0_1;

import fr.utarwyn.endercontainers.migration.Migration;

import java.util.logging.Level;
import java.util.regex.Pattern;

public abstract class Migration2_0_1 extends Migration {

	private static Pattern BASE64_PATTERN = Pattern.compile("([A-Za-z0-9+/]{4})*([A-Za-z0-9+/]{4}|[A-Za-z0-9+/]{3}=|[A-Za-z0-9+/]{2}==)");

	public Migration2_0_1() {
		super("2.0.0", "2.0.1");
	}

	@Override
	public void perform() {
		/* ------------------------- */
		/*  Update configuration...  */
		/* ------------------------- */
		this.logger.info("Update old configuration...");
		if (!this.updateConfiguration()) return;

		/* ---------------------------- */
		/*  Reconfigure chests data...  */
		/* ---------------------------- */
		this.logger.info("Reconfigure chests content...");
		try {
			this.reconfigureChestsContent();
		} catch (Exception e) {
			this.logger.log(Level.SEVERE, "Cannot reconfigure chests content", e);
		}
	}

	abstract void reconfigureChestsContent() throws Exception;

	boolean isBase64Encoded(String data) {
		return BASE64_PATTERN.matcher(data).matches();
	}

}

package fr.utarwyn.endercontainers.migration.migration2_0_3;

import fr.utarwyn.endercontainers.migration.Migration;
import fr.utarwyn.endercontainers.util.Log;

public abstract class Migration2_0_3 extends Migration {

	Migration2_0_3() {
		super("2.0.2", "2.0.3");
	}

	@Override
	public void perform() {
		/* ------------------------- */
		/*  Update configuration...  */
		/* ------------------------- */
		Log.log("Update old configuration...", true);
		this.updateConfiguration();
	}

}

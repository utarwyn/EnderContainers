package fr.utarwyn.endercontainers.migration.migration2_0_3;

import fr.utarwyn.endercontainers.migration.Migration;

public class Migration2_0_3 extends Migration {

    public Migration2_0_3() {
        super("2.0.2", "2.0.3");
    }

    @Override
    public void perform() {
        /* ------------------------- */
        /*  Update configuration...  */
        /* ------------------------- */
        this.logger.info("Update old configuration...");
        this.updateConfiguration();
    }

}

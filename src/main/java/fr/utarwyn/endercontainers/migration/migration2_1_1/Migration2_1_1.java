package fr.utarwyn.endercontainers.migration.migration2_1_1;

import fr.utarwyn.endercontainers.migration.Migration;

public class Migration2_1_1 extends Migration {

    public Migration2_1_1() {
        super("2.1.0", "2.1.1");
    }

    @Override
    public void perform() {
        /* ------------------- */
        /*  Update locales...  */
        /* ------------------- */
        this.logger.info("Update locales...");
        this.updateLocales();
    }

    private void updateLocales() {
        // TODO: I need to add to all locale files new keys next/previous page for menus and cmd errors
    }

}

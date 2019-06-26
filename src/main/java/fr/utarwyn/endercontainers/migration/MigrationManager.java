package fr.utarwyn.endercontainers.migration;

import fr.utarwyn.endercontainers.AbstractManager;
import fr.utarwyn.endercontainers.EnderContainers;
import fr.utarwyn.endercontainers.configuration.Files;
import fr.utarwyn.endercontainers.migration.migration2_0_1.Migration2_0_1;
import fr.utarwyn.endercontainers.migration.migration2_0_3.Migration2_0_3;
import fr.utarwyn.endercontainers.migration.migration2_1_1.Migration2_1_1;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

/**
 * Manages, loads and runs migrations for the plugin
 *
 * @author Utarwyn
 * @since 2.0.0
 */
public class MigrationManager extends AbstractManager {

    /**
     * Stores the name of the version file
     */
    static final String VERSION_FILE = "version";

    /**
     * Contains loaded migrations which can be runned
     */
    private List<Migration> migrations;

    /**
     * State of the manager. True if a migration have been applied
     */
    private boolean migrationDone;

    /**
     * {@inheritDoc}
     */
    @Override
    public void load() {
        this.migrations = new ArrayList<>();
        this.loadMigration(Migration2_0_1.class);
        this.loadMigration(Migration2_0_3.class);
        this.loadMigration(Migration2_1_1.class);

        for (Migration migration : this.migrations) {
            if (!this.migrationDone && migration.hasToBePerformed()) {
                this.performMigration(migration);
            }
        }

        if (this.migrationDone) {
            this.logger.info("Please restart the server to enable the plugin!");
            this.logger.info("If you have any error after the restart, please contact the plugin's author!");
        }

        // Write the current version of the plugin in the "version" file
        this.writeVersion(EnderContainers.getInstance().getDescription().getVersion());
    }

    /**
     * Returns if the manager have applied a migration or not
     *
     * @return True if a migration have been applied
     */
    public boolean hasDoneMigration() {
        return this.migrationDone;
    }

    /**
     * Load a migration with a given class.
     * This method try to load the storage specific migration by default.
     * Otherwise it tries to load the general migration for both storages.
     *
     * @param clazz Class used to load the migration
     */
    private void loadMigration(Class<? extends Migration> clazz) {
        Class<? extends Migration> migrationClazz;
        String storagePath = Files.getConfiguration().isMysql() ? "MySQL" : "Flat";

        // Try to load the storage specific migration class
        try {
            migrationClazz = (Class<? extends Migration>) Class.forName(clazz.getName().replace("Migration", "Migration" + storagePath));
        } catch (ClassNotFoundException ignored) {
            migrationClazz = null;
        }

        // Otherwise use only the general migration class
        if (migrationClazz == null) {
            migrationClazz = clazz;
        }

        try {
            this.migrations.add(migrationClazz.newInstance());
        } catch (InstantiationException | IllegalAccessException e) {
            this.logger.log(Level.SEVERE, "Cannot instanciate the migration class " + clazz.getName(), e);
        }
    }

    /**
     * Perform a specific migration!
     *
     * @param migration Migration to perform
     */
    private void performMigration(Migration migration) {
        // Announce it in the console
        this.logger.warning("ALERT! A migration have to be applied before continue using the plugin!");
        this.logger.log(
                Level.INFO, "Migration detected from version {0} to version {1}!",
                new Object[]{migration.getFromVers(), migration.getToVers()}
        );
        this.logger.info("Starting migration...");

        // Prepare and perform the migration!
        migration.prepare();
        migration.perform();

        // End message
        this.logger.info("");
        this.logger.log(
                Level.INFO, "Migration from {0} to {1} has been performed!",
                new Object[]{migration.getFromVers(), migration.getToVers()}
        );

        this.migrationDone = true;
    }

    /**
     * Writes the current version of the plugin in a file
     */
    private void writeVersion(String version) {
        File versionFile = new File(EnderContainers.getInstance().getDataFolder(), VERSION_FILE);

        try {
            if (!versionFile.exists() && !versionFile.createNewFile())
                return;

            PrintWriter pw = new PrintWriter(new FileOutputStream(versionFile));

            pw.flush();
            pw.print(version);
            pw.close();
        } catch (IOException e) {
            this.logger.log(Level.SEVERE, "Cannot write the file \"version\"", e);
        }
    }

}

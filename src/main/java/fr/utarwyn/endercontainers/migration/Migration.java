package fr.utarwyn.endercontainers.migration;

import fr.utarwyn.endercontainers.EnderContainers;
import fr.utarwyn.endercontainers.database.Database;
import fr.utarwyn.endercontainers.database.DatabaseManager;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * Represents a migration which can be executed
 *
 * @author Utarwyn
 * @since 2.0.0
 */
public abstract class Migration {

    /**
     * Database object used for MySQL migrations
     */
    private static Database database;
    /**
     * The plugin logger
     */
    protected Logger logger;
    /**
     * Old version of the plugin that the migration needs to be runned
     */
    private String fromVers;
    /**
     * New version of the plugin that the migration needs to be runned
     */
    private String toVers;
    /**
     * Current version of the plugin (stored inside the description file)
     */
    private String pluginVersion;
    /**
     * Version of stored data of the plugin
     */
    private String dataVersion;

    /**
     * Constructs a new migration
     *
     * @param fromVers Old version of the plugin for detections
     * @param toVers   New version of the plugin for detections
     */
    public Migration(String fromVers, String toVers) {
        this.fromVers = fromVers;
        this.toVers = toVers;
        this.pluginVersion = EnderContainers.getInstance().getDescription().getVersion();
        this.logger = EnderContainers.getInstance().getLogger();
    }

    /**
     * Gets the database object for MySQL migrations.
     * <p>
     * This method devious the normal access of the database object
     * to have a global access to the database.
     * (Because migrations normally do special requests to upgrade tables)
     * </p>
     *
     * @return The database object
     */
    // TODO: 28/04/2019 OMG, remove this method because its very ugly!
    protected static Database getDatabase() {
        if (database != null)
            return database;

        DatabaseManager manager = EnderContainers.getInstance().getManager(DatabaseManager.class);
        if (!manager.isReady()) return null;

        // Not very clear but its necessary to modify tables like we want
        try {
            Field f = manager.getClass().getDeclaredField("database");

            f.setAccessible(true);
            database = (Database) f.get(manager);
            f.setAccessible(false);

            return database;
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Do a recursive copy from a folder to another folder
     *
     * @param fSource Source folder
     * @param fDest   Destination folder
     */
    private static void recursiveCopy(File fSource, File fDest) throws IOException {
        if (fSource.isDirectory()) {
            if (!fDest.exists() && !fDest.mkdirs()) {
                return;
            }

            // Create list of files and directories on the current source
            String[] fList = fSource.list();

            if (fList != null) {
                for (String aFList : fList) {
                    File dest = new File(fDest, aFList);
                    File source = new File(fSource, aFList);

                    if (!aFList.equals(fDest.getName())) {
                        recursiveCopy(source, dest);
                    }
                }
            }
        } else {
            // Found a file. Copy it into the destination, which is already created in 'if' condition above
            try (FileInputStream inputStream = new FileInputStream(fSource);
                 FileOutputStream outputStream = new FileOutputStream(fDest)) {
                byte[] buffer = new byte[2048];
                int iBytesReads;

                while ((iBytesReads = inputStream.read(buffer)) >= 0) {
                    outputStream.write(buffer, 0, iBytesReads);
                }
            }
        }
    }

    String getFromVers() {
        return this.fromVers;
    }

    String getToVers() {
        return this.toVers;
    }

    /**
     * Calculates if the migration has to be runned or not
     *
     * @return True if the migration has to be runned
     */
    boolean hasToBePerformed() {
        Pattern pFrom = Pattern.compile(this.fromVers);
        Pattern pTo = Pattern.compile(this.toVers);

        return pFrom.matcher(this.getDataVersion()).find() && pTo.matcher(this.pluginVersion).find();
    }

    /**
     * Gets the data folder of the plugin
     *
     * @return Data folder of the plugin
     */
    private File getDataFolder() {
        return EnderContainers.getInstance().getDataFolder();
    }

    /**
     * Prepares the migration
     */
    void prepare() {
        File backupFolder = this.getBackupFolder();

        // Anounce the creation of a backup folder for the migration
        this.logger.info(" ");
        this.logger.info("|----------------------------------|");
        this.logger.info(String.format("| Migration from %5s to %5s    |", this.fromVers, this.toVers));
        this.logger.info("|----------------------------------|");
        this.logger.info(" ");
        this.logger.info("Backuping data files into the \"EnderContainers/" + backupFolder.getName() + "/\" folder...");
        this.logger.info(" ");

        // Create the backup folder
        try {
            Migration.recursiveCopy(this.getDataFolder(), backupFolder);
        } catch (IOException e) {
            this.logger.log(Level.SEVERE, "Cannot copy all data to the backup folder", e);
        }
    }

    /**
     * Called to perform the migration
     */
    public abstract void perform();

    /**
     * Updates the configuration with the old configuration.
     * This method keeps configuration comments in the Yaml file!
     *
     * @return True if the configuration has been updated
     */
    protected boolean updateConfiguration() {
        File confFile = new File(this.getDataFolder(), "config.yml");
        YamlConfiguration config = YamlConfiguration.loadConfiguration(confFile);

        EnderContainers.getInstance().saveResource("config.yml", true);
        YamlNewConfiguration newConfig = YamlNewConfiguration.loadConfiguration(confFile);

        newConfig.applyConfiguration(config);

        try {
            newConfig.save(confFile);
        } catch (IOException e) {
            this.logger.log(Level.SEVERE, "Cannot save the updated configuration on the disk", e);
            return false;
        }

        return true;
    }

    /**
     * Returns all files in which there are chests configurations
     *
     * @return Enderchests files
     */
    protected List<File> getChestFiles() {
        // Get all normal chest files
        List<File> files = new ArrayList<>(Arrays.asList(
                Objects.requireNonNull(new File(this.getDataFolder(), "data/").listFiles())
        ));

        // Add all backups chests
        File globalBackupsFolder = new File(this.getDataFolder(), "backups/");

        if (globalBackupsFolder.isDirectory()) {
            for (File backupFolder : Objects.requireNonNull(globalBackupsFolder.listFiles())) {
                if (backupFolder.isDirectory()) {
                    files.addAll(Arrays.asList(Objects.requireNonNull(backupFolder.listFiles())));
                }
            }
        }

        return files;
    }

    /**
     * Gets the File object of the backup folder for the migration
     *
     * @return Backup folder for the migration
     */
    private File getBackupFolder() {
        return new File(this.getDataFolder(), "old_" + this.fromVers.replaceAll("\\*", "X").replaceAll("\\.", "_") + "/");
    }

    /**
     * Gets the current version of stored data
     *
     * @return Version of stored data (for EnderContainers)
     */
    private String getDataVersion() {
        if (this.dataVersion != null) {
            return this.dataVersion;
        }

        // By default, gets the version from the version file in the data folder
        File file = new File(EnderContainers.getInstance().getDataFolder(), MigrationManager.VERSION_FILE);

        if (file.exists()) {
            try (FileInputStream inputStream = new FileInputStream(file); Scanner scanner = new Scanner(inputStream)) {
                this.dataVersion = scanner.nextLine();
                return this.dataVersion;
            } catch (IOException e) {
                this.logger.log(Level.SEVERE, "Cannot get the local version of the plugin!", e);
                this.logger.warning("You can create a file \"version\" in the plugin folder " +
                        "with the version \"" + this.pluginVersion + "\" in it to fix this error.");
            }
        }

        // If no version found, use the saved one!
        return this.pluginVersion;
    }

}

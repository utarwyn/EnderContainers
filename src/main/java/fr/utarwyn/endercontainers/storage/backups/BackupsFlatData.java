package fr.utarwyn.endercontainers.storage.backups;

import fr.utarwyn.endercontainers.EnderContainers;
import fr.utarwyn.endercontainers.backup.Backup;
import fr.utarwyn.endercontainers.storage.FlatFile;
import fr.utarwyn.endercontainers.util.EUtil;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.logging.Level;

/**
 * Storage wrapper for backups (flatfile)
 *
 * @author Utarwyn
 * @since 2.0.0
 */
public class BackupsFlatData extends BackupsData {

    private static final String CONF_PREFIX = "backups";

    private FlatFile flatFile;

    BackupsFlatData() {
        this.backups = new ArrayList<>();
        this.load();
    }

    @Override
    protected void load() {
        try {
            this.flatFile = new FlatFile("backups.yml");
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Cannot load the backups file", e);
        }

        if (!this.flatFile.getConfiguration().isConfigurationSection(CONF_PREFIX)) {
            this.flatFile.getConfiguration().set(CONF_PREFIX, new ArrayList<>());
        }

        YamlConfiguration config = this.flatFile.getConfiguration();

        // No backup there!
        if (config.getConfigurationSection(CONF_PREFIX) == null)
            return;

        for (String key : config.getConfigurationSection(CONF_PREFIX).getKeys(false)) {
            String name = config.getString(CONF_PREFIX + "." + key + ".name");
            Timestamp date = new Timestamp(config.getLong(CONF_PREFIX + "." + key + ".date"));
            String type = config.getString(CONF_PREFIX + "." + key + ".type");
            String createdBy = config.getString(CONF_PREFIX + "." + key + ".createdBy");

            this.backups.add(new Backup(name, date, type, createdBy));
        }
    }

    @Override
    protected void save() {
        try {
            this.flatFile.save();
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Cannot save the backups file", e);
        }
    }

    @Override
    public boolean saveNewBackup(Backup backup) {
        YamlConfiguration config = this.flatFile.getConfiguration();
        String name = backup.getName();

        config.set(CONF_PREFIX + "." + name + ".name", name);
        config.set(CONF_PREFIX + "." + name + ".date", backup.getDate().getTime());
        config.set(CONF_PREFIX + "." + name + ".type", backup.getType());
        config.set(CONF_PREFIX + "." + name + ".createdBy", backup.getCreatedBy());

        this.save();
        return true;
    }

    @Override
    public boolean executeStorage(Backup backup) {
        File folder = new File(EnderContainers.getInstance().getDataFolder(), "backups/" + backup.getName() + "/");

        // Create base folder if not exists.
        if (!folder.exists() && !folder.mkdirs())
            return false;

        File enderFolder = new File(EnderContainers.getInstance().getDataFolder(), "data");

        return this.copyFolderFiles(enderFolder, folder);
    }

    @Override
    public boolean applyBackup(Backup backup) {
        File folder = new File(EnderContainers.getInstance().getDataFolder(), "backups/" + backup.getName() + "/");
        if (!folder.exists()) return false;
        File enderFolder = new File(EnderContainers.getInstance().getDataFolder(), "data");

        return this.copyFolderFiles(folder, enderFolder);
    }

    @Override
    public boolean removeBackup(Backup backup) {
        // Delete backup folder.
        File folder = new File(EnderContainers.getInstance().getDataFolder(), "backups/" + backup.getName() + "/");
        if (folder.exists() && !EUtil.deleteFolder(folder))
            return false;

        // Remove the backup from the configuration file.
        this.flatFile.getConfiguration().set(CONF_PREFIX + "." + backup.getName(), null);

        // Save the config file.
        this.save();
        return true;
    }

    /**
     * Copy all files from a folder to a different folder
     *
     * @param from Source folder
     * @param to   Destination folder
     * @return True if all files have been copied in destination folder.
     */
    private boolean copyFolderFiles(File from, File to) {
        File[] filesFrom = from.listFiles();
        if (filesFrom == null) return false;

        for (File fileFrom : filesFrom) {
            if (fileFrom.getName().contains(".")) {
                File fileTo = new File(to, fileFrom.getName());
                try {
                    Files.copy(fileFrom.toPath(), fileTo.toPath(), StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException e) {
                    logger.log(Level.SEVERE, String.format("Cannot copy file %s to %s", fileFrom.toPath(), fileTo.toPath()), e);
                    return false;
                }
            }
        }

        return true;
    }

}

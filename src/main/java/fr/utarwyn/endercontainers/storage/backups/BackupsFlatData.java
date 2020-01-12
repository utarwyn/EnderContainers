package fr.utarwyn.endercontainers.storage.backups;

import fr.utarwyn.endercontainers.EnderContainers;
import fr.utarwyn.endercontainers.backup.Backup;
import fr.utarwyn.endercontainers.storage.FlatFile;
import fr.utarwyn.endercontainers.util.MiscUtil;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Storage wrapper for backups (flatfile)
 *
 * @author Utarwyn
 * @since 2.0.0
 */
public class BackupsFlatData extends BackupsData {

    private static final String PREFIX = "backups";

    /**
     * Object which manages interaction with a flat file
     */
    private FlatFile flatFile;

    /**
     * Construct a new backup storage wrapper with a flat file.
     *
     * @param logger plugin logger
     */
    public BackupsFlatData(Logger logger) {
        super(logger);
        this.backups = new ArrayList<>();
        this.load();
    }

    /**
     * Get the folder which used to store files of a specific backup.
     *
     * @param backup Backup used to get the folder path
     * @return Folder where backup files are stored
     */
    private static File getBackupFolder(Backup backup) {
        return new File(
                EnderContainers.getInstance().getDataFolder(),
                PREFIX + File.separator + backup.getName()
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void load() {
        try {
            this.flatFile = new FlatFile("backups.yml");
        } catch (IOException e) {
            this.logger.log(Level.SEVERE, "Cannot load the backups file", e);
        }

        if (!this.flatFile.getConfiguration().isConfigurationSection(PREFIX)) {
            this.flatFile.getConfiguration().set(PREFIX, new ArrayList<>());
        }

        YamlConfiguration config = this.flatFile.getConfiguration();
        ConfigurationSection section = config.getConfigurationSection(PREFIX);

        if (section != null) {
            for (String key : section.getKeys(false)) {
                String name = config.getString(PREFIX + "." + key + ".name");
                Timestamp date = new Timestamp(config.getLong(PREFIX + "." + key + ".date"));
                String type = config.getString(PREFIX + "." + key + ".type");
                String createdBy = config.getString(PREFIX + "." + key + ".createdBy");

                this.backups.add(new Backup(name, date, type, createdBy));
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void save() {
        try {
            this.flatFile.save();
        } catch (IOException e) {
            this.logger.log(Level.SEVERE, "Cannot save the backups file", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean saveNewBackup(Backup backup) {
        YamlConfiguration config = this.flatFile.getConfiguration();
        String name = backup.getName();

        config.set(PREFIX + "." + name + ".name", name);
        config.set(PREFIX + "." + name + ".date", backup.getDate().getTime());
        config.set(PREFIX + "." + name + ".type", backup.getType());
        config.set(PREFIX + "." + name + ".createdBy", backup.getCreatedBy());

        this.save();
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean executeStorage(Backup backup) {
        File folder = BackupsFlatData.getBackupFolder(backup);

        if (!folder.exists() && !folder.mkdirs()) {
            return false;
        }

        File enderFolder = new File(EnderContainers.getInstance().getDataFolder(), "data");
        return this.copyFolderFiles(enderFolder, folder);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean applyBackup(Backup backup) {
        File folder = BackupsFlatData.getBackupFolder(backup);

        if (folder.exists()) {
            File enderFolder = new File(EnderContainers.getInstance().getDataFolder(), "data");
            return this.copyFolderFiles(folder, enderFolder);
        }

        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean removeBackup(Backup backup) {
        File folder = BackupsFlatData.getBackupFolder(backup);

        if (folder.exists() && !MiscUtil.deleteFolder(folder)) {
            return false;
        }

        this.flatFile.getConfiguration().set(PREFIX + "." + backup.getName(), null);
        this.save();

        return true;
    }

    /**
     * Copy all files from a folder to a different folder.
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
                    this.logger.log(Level.SEVERE, String.format("Cannot copy file %s to %s", fileFrom.toPath(), fileTo.toPath()), e);
                    return false;
                }
            }
        }

        return true;
    }

}

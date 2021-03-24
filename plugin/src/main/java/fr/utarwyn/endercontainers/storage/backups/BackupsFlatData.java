package fr.utarwyn.endercontainers.storage.backups;

import fr.utarwyn.endercontainers.EnderContainers;
import fr.utarwyn.endercontainers.backup.Backup;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.logging.Level;

/**
 * Storage wrapper to manage backups through a Yaml file.
 *
 * @author Utarwyn
 * @since 2.0.0
 */
public class BackupsFlatData extends BackupsData {

    /**
     * Prefix used in the config file to store backups
     */
    private static final String PREFIX = "backups";

    /**
     * Storage file native object
     */
    private final File file;

    /**
     * Configuration file object of backups
     */
    FileConfiguration configuration;

    /**
     * Construct a new backup storage wrapper with a flat file.
     *
     * @param plugin plugin instance object
     */
    public BackupsFlatData(EnderContainers plugin) {
        super(plugin);

        this.backups = new ArrayList<>();
        this.file = new File(this.plugin.getDataFolder(), "backups.yml");

        this.load();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void load() {
        this.configuration = YamlConfiguration.loadConfiguration(this.file);

        ConfigurationSection section = this.configuration.getConfigurationSection(PREFIX);
        if (section != null) {
            for (String key : section.getKeys(false)) {
                String name = section.getString(key + ".name");
                Timestamp date = new Timestamp(section.getLong(key + ".date"));
                String createdBy = section.getString(key + ".createdBy");

                this.backups.add(new Backup(name, date, createdBy));
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void save() {
        try {
            this.configuration.save(this.file);
        } catch (IOException e) {
            this.plugin.getLogger().log(Level.SEVERE, String.format(
                    "Cannot save backups to %s", this.file.getPath()
            ), e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean saveNewBackup(Backup backup) {
        String name = backup.getName();

        this.configuration.set(PREFIX + "." + name + ".name", name);
        this.configuration.set(PREFIX + "." + name + ".date", backup.getDate().getTime());
        this.configuration.set(PREFIX + "." + name + ".createdBy", backup.getCreatedBy());
        this.save();

        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean executeStorage(Backup backup) {
        File folder = this.getBackupFolder(backup);

        if (!folder.exists() && !folder.mkdirs()) {
            return false;
        }

        File enderFolder = new File(this.plugin.getDataFolder(), "data");
        return this.copyFolderFiles(enderFolder, folder);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean applyBackup(Backup backup) {
        File folder = this.getBackupFolder(backup);

        if (folder.exists()) {
            File enderFolder = new File(this.plugin.getDataFolder(), "data");
            return this.copyFolderFiles(folder, enderFolder);
        }

        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean removeBackup(Backup backup) {
        File folder = this.getBackupFolder(backup);

        try {
            if (folder.isDirectory()) {
                deleteFolder(folder);
            }

            this.configuration.set(PREFIX + "." + backup.getName(), null);
            this.save();

            return true;
        } catch (IOException e) {
            this.plugin.getLogger().log(Level.SEVERE, String.format(
                    "Cannot delete backup folder %s", folder.getPath()
            ), e);
        }

        return false;
    }

    /**
     * Get the folder which used to store files of a specific backup.
     *
     * @param backup Backup used to get the folder path
     * @return Folder where backup files are stored
     */
    private File getBackupFolder(Backup backup) {
        return new File(this.plugin.getDataFolder(),
                PREFIX + File.separator + backup.getName());
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
                    Files.copy(
                            fileFrom.toPath(), fileTo.toPath(),
                            StandardCopyOption.REPLACE_EXISTING
                    );
                } catch (IOException e) {
                    this.plugin.getLogger().log(Level.SEVERE, String.format(
                            "Cannot copy file %s to %s", fileFrom.toPath(), fileTo.toPath()
                    ), e);
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * Delete a folder recursively with all files inside of it.
     *
     * @param folder folder to delete
     * @throws IOException if a folder or file cannot be deleted
     */
    private static void deleteFolder(File folder) throws IOException {
        File[] files = folder.listFiles();
        if (files == null) {
            Files.delete(folder.toPath());
            return;
        }

        for (File file : files) {
            if (file.isDirectory()) {
                deleteFolder(file);
            } else {
                Files.delete(file.toPath());
            }
        }

        Files.delete(folder.toPath());
    }

}

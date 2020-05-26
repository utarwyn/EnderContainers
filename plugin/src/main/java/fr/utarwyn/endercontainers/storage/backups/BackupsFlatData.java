package fr.utarwyn.endercontainers.storage.backups;

import fr.utarwyn.endercontainers.EnderContainers;
import fr.utarwyn.endercontainers.backup.Backup;
import fr.utarwyn.endercontainers.configuration.wrapper.YamlFileLoadException;
import fr.utarwyn.endercontainers.configuration.wrapper.YamlFileWrapper;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;

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
     * Object which manages backups configuration
     */
    private final YamlFileWrapper configuration;

    /**
     * Construct a new backup storage wrapper with a flat file.
     *
     * @param plugin plugin instance object
     */
    public BackupsFlatData(EnderContainers plugin) {
        super(plugin);

        this.backups = new ArrayList<>();
        this.configuration = new YamlFileWrapper(
                new File(this.plugin.getDataFolder(), "backups.yml")
        );

        this.load();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void load() {
        try {
            this.configuration.load();
        } catch (YamlFileLoadException e) {
            this.plugin.getLogger().log(Level.SEVERE, "Cannot load the backups file", e);
            return;
        }

        if (!this.configuration.get().isConfigurationSection(PREFIX)) {
            this.configuration.get().set(PREFIX, new ArrayList<>());
        }

        ConfigurationSection section = this.configuration.get().getConfigurationSection(PREFIX);

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
            this.configuration.save();
        } catch (IOException e) {
            this.plugin.getLogger().log(Level.SEVERE, "Cannot save the backups file", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean saveNewBackup(Backup backup) {
        Configuration config = this.configuration.get();
        String name = backup.getName();

        config.set(PREFIX + "." + name + ".name", name);
        config.set(PREFIX + "." + name + ".date", backup.getDate().getTime());
        config.set(PREFIX + "." + name + ".createdBy", backup.getCreatedBy());

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

            this.configuration.get().set(PREFIX + "." + backup.getName(), null);
            this.save();

            return true;
        } catch (IOException e) {
            this.plugin.getLogger().log(Level.SEVERE,
                    String.format("Cannot delete the folder: %s", folder), e);
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

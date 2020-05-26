package fr.utarwyn.endercontainers.configuration.wrapper;

import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

/**
 * Wrapper class that manages a Yaml configuration file stored on the disk.
 *
 * @author Utarwyn <maximemalgorn@gmail.com>
 * @link https://en.wikipedia.org/wiki/YAML
 * @since 2.2.0
 */
public class YamlFileWrapper {

    /**
     * Java file object representation
     */
    private final File file;

    /**
     * URL of a default resource to use when initializing the file
     */
    private final URL defaultResource;

    /**
     * Configuration object used to retrieve and format the config file
     */
    protected FileConfiguration configuration;

    /**
     * Constructs a new configuration file wrapper without default resource.
     *
     * @param file Java file object
     */
    public YamlFileWrapper(File file) {
        this(file, null);
    }

    /**
     * Constructs a new configuration file wrapper with a default resource.
     *
     * @param file            Java file object
     * @param defaultResource default resource URL to use when initializing
     */
    public YamlFileWrapper(File file, URL defaultResource) {
        this.file = file;
        this.defaultResource = defaultResource;
    }

    /**
     * Retrieves the configuration object.
     *
     * @return configuration object
     */
    public Configuration get() {
        if (this.configuration == null) {
            throw new NullPointerException("configuration object of the file is null");
        }

        return this.configuration;
    }

    /**
     * Loads the configuration file from the disk.
     * Create the file on the disk with the default resource if provided.
     *
     * @throws YamlFileLoadException thrown if the configuration file cannot be loaded
     */
    public void load() throws YamlFileLoadException {
        this.configuration = this.createConfiguration(this.file);

        if (this.defaultResource != null) {
            try {
                // Copy it if the configuration file does not exists
                if (!this.file.isFile()) {
                    Files.copy(this.getDefaultResourceStream(), this.file.toPath());
                }

                // And set it to the configuration defaults
                this.configuration.setDefaults(YamlConfiguration.loadConfiguration(
                        new InputStreamReader(this.getDefaultResourceStream(),
                                StandardCharsets.UTF_8)
                ));
            } catch (IOException e) {
                throw new YamlFileLoadException(
                        "Cannot copy the default resource on the disk", e);
            }
        }
    }

    /**
     * Saves the configuration file on the disk.
     *
     * @throws IOException thrown if the configuration file cannot be saved
     */
    public void save() throws IOException {
        if (this.configuration != null) {
            this.configuration.save(this.file);
        }
    }

    /**
     * Retrieves and creates the configuration object from a file.
     *
     * @param file file object
     * @return created configuration instance from the file data
     */
    protected FileConfiguration createConfiguration(File file) {
        return YamlConfiguration.loadConfiguration(file);
    }

    /**
     * Creates a stream with the content of the default resource.
     *
     * @return input stream with the resource content
     */
    private InputStream getDefaultResourceStream() throws IOException {
        URLConnection connection = this.defaultResource.openConnection();
        connection.setUseCaches(false);
        return connection.getInputStream();
    }

}

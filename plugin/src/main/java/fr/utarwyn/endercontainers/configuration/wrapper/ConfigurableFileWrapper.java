package fr.utarwyn.endercontainers.configuration.wrapper;

import fr.utarwyn.endercontainers.configuration.Configurable;

import java.io.File;
import java.lang.reflect.Field;
import java.net.URL;

/**
 * Represents a dynamically configurable
 * wrapper for a configuration file on the disk.
 *
 * @author Utarwyn <maximemalgorn@gmail.com>
 * @since 2.2.0
 */
public abstract class ConfigurableFileWrapper extends YamlFileWrapper {

    /**
     * Constructs the configurable file wrapper.
     *
     * @param file file object
     */
    protected ConfigurableFileWrapper(File file) {
        super(file);
    }

    /**
     * Constructs the configurable file wrapper with a default resource.
     *
     * @param file            file object
     * @param defaultResource default resource URL
     */
    protected ConfigurableFileWrapper(File file, URL defaultResource) {
        super(file, defaultResource);
    }

    /**
     * Loads the configuration file from the disk.
     * Fills attributes of the instance with values from the file.
     *
     * @throws YamlFileLoadException thrown if the configuration file cannot be loaded
     */
    @Override
    public void load() throws YamlFileLoadException {
        super.load();

        // Load every needed config value dynamically!
        for (Field field : this.getClass().getDeclaredFields()) {
            Configurable conf = field.getAnnotation(Configurable.class);
            if (conf == null) continue;

            // Getting the config key associated with the field
            String configKey = (conf.key().isEmpty()) ? field.getName() : conf.key();
            Object value = this.configuration.get(configKey);

            // Changing the value of the field
            try {
                field.setAccessible(true);
                field.set(this, value);
                field.setAccessible(false);
            } catch (ReflectiveOperationException | IllegalArgumentException e) {
                String configName = getClass().getSimpleName().toLowerCase();
                throw new YamlFileLoadException(String.format(
                        "Cannot set the config value %s of key %s for %s",
                        value, configKey, configName
                ), e);
            }
        }
    }

}

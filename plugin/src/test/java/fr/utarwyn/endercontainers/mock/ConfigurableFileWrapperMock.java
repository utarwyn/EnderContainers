package fr.utarwyn.endercontainers.mock;

import fr.utarwyn.endercontainers.configuration.Configurable;
import fr.utarwyn.endercontainers.configuration.wrapper.ConfigurableFileWrapper;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;

public class ConfigurableFileWrapperMock extends ConfigurableFileWrapper {

    @Configurable
    public String field1;

    @Configurable(key = "field2.custom_field")
    public String field2;

    public Integer field3;

    public ConfigurableFileWrapperMock(FileConfiguration configuration) {
        super(null);
        this.configuration = configuration;
    }

    @Override
    protected FileConfiguration createConfiguration(File file) {
        return this.configuration;
    }

}

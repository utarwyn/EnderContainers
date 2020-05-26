package fr.utarwyn.endercontainers.mock;

import fr.utarwyn.endercontainers.configuration.wrapper.YamlFileWrapper;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.net.URL;

public class YamlFileWrapperMock extends YamlFileWrapper {

    public YamlFileWrapperMock(FileConfiguration configuration, File file) {
        super(file);
        this.configuration = configuration;
    }

    public YamlFileWrapperMock(FileConfiguration configuration, File file,
                               URL defaultResource) {
        super(file, defaultResource);
        this.configuration = configuration;
    }

    @Override
    protected FileConfiguration createConfiguration(File file) {
        return this.configuration;
    }

}

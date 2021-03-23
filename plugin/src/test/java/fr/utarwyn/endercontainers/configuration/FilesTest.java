package fr.utarwyn.endercontainers.configuration;

import fr.utarwyn.endercontainers.TestHelper;
import fr.utarwyn.endercontainers.TestInitializationException;
import fr.utarwyn.endercontainers.configuration.wrapper.YamlFileLoadException;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@RunWith(MockitoJUnitRunner.class)
public class FilesTest {

    @BeforeClass
    public static void setUpClass() throws TestInitializationException {
        TestHelper.setUpFiles();
    }

    @Test
    public void configuration() throws YamlFileLoadException {
        assertThat(Files.getConfiguration()).isNotNull();
        Files.initConfiguration(mock(JavaPlugin.class));
    }

    @Test
    public void locale() throws YamlFileLoadException {
        assertThat(Files.getLocale()).isNotNull();
        Files.initLocale(mock(JavaPlugin.class));
    }

}

package fr.utarwyn.endercontainers.configuration;

import fr.utarwyn.endercontainers.TestHelper;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@RunWith(MockitoJUnitRunner.class)
public class FilesTest {

    @BeforeClass
    public static void setUpClass() throws IOException,
            InvalidConfigurationException, ReflectiveOperationException {
        TestHelper.setUpFiles();
    }

    @Test
    public void configuration() {
        assertThat(Files.getConfiguration()).isNotNull();
        assertThat(Files.initConfiguration(mock(JavaPlugin.class))).isFalse();
    }

    @Test
    public void locale() {
        assertThat(Files.getLocale()).isNotNull();
        assertThat(Files.initLocale(mock(JavaPlugin.class))).isFalse();
    }

}

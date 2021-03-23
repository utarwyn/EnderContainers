package fr.utarwyn.endercontainers.configuration.wrapper;

import fr.utarwyn.endercontainers.TestHelper;
import fr.utarwyn.endercontainers.TestInitializationException;
import fr.utarwyn.endercontainers.mock.ConfigurableFileWrapperMock;
import org.bukkit.configuration.file.FileConfiguration;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ConfigurableFileWrapperTest {

    @BeforeClass
    public static void setUpClass() throws TestInitializationException {
        TestHelper.setUpFiles();
    }

    @Test
    public void load() {
        FileConfiguration configuration = mock(FileConfiguration.class);
        ConfigurableFileWrapperMock wrapper = new ConfigurableFileWrapperMock(configuration);

        when(configuration.get("field1")).thenReturn("hello world");
        when(configuration.get("field2.custom_field")).thenReturn("awesome test");

        try {
            wrapper.load();

            assertThat(wrapper.field1).isEqualTo("hello world");
            assertThat(wrapper.field2).isEqualTo("awesome test");
            assertThat(wrapper.field3).isNull();
        } catch (YamlFileLoadException e) {
            fail("should not create an error during file loading", e);
        }
    }

}

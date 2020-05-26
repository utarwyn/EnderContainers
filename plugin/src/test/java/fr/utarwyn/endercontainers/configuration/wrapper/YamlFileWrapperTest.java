package fr.utarwyn.endercontainers.configuration.wrapper;

import fr.utarwyn.endercontainers.TestHelper;
import fr.utarwyn.endercontainers.mock.YamlFileWrapperMock;
import org.bukkit.configuration.file.FileConfiguration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class YamlFileWrapperTest {

    @Test
    public void getWithoutConfiguration() {
        YamlFileWrapper wrapper = new YamlFileWrapperMock(null, null);
        assertThatNullPointerException().isThrownBy(wrapper::get)
                .withNoCause()
                .withMessage("configuration object of the file is null");
    }

    @Test
    public void load() {
        FileConfiguration configuration = mock(FileConfiguration.class);
        YamlFileWrapper wrapper = new YamlFileWrapperMock(configuration, null);

        try {
            wrapper.load();
            assertThat(wrapper.configuration).isNotNull().isEqualTo(configuration);
            assertThat(wrapper.get()).isNotNull().isEqualTo(configuration);
        } catch (YamlFileLoadException e) {
            fail("should not create an error during file loading", e);
        }
    }

    @Test
    public void loadWithDefaultResource() {
        File file = mock(File.class);
        URL defaultResource = TestHelper.class.getResource("/locale.yml");
        FileConfiguration configuration = mock(FileConfiguration.class);
        YamlFileWrapper wrapper = new YamlFileWrapperMock(configuration, file, defaultResource);

        when(file.isFile()).thenReturn(true);

        try {
            wrapper.load();

            assertThat(wrapper.configuration).isNotNull().isEqualTo(configuration);
            assertThat(wrapper.get()).isNotNull().isEqualTo(configuration);
            verify(configuration, times(1)).setDefaults(any());
        } catch (YamlFileLoadException e) {
            fail("should not create an error during file loading", e);
        }
    }

    @Test
    public void save() throws IOException {
        // Without configuration
        YamlFileWrapper wrapper = new YamlFileWrapperMock(null, null);
        wrapper.save();

        // With a mocked configuration
        File file = mock(File.class);
        FileConfiguration configuration = mock(FileConfiguration.class);

        wrapper = new YamlFileWrapperMock(configuration, file);
        wrapper.save();

        verify(configuration, times(1)).save(file);
    }

}

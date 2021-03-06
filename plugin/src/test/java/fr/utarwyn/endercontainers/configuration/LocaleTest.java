package fr.utarwyn.endercontainers.configuration;

import fr.utarwyn.endercontainers.EnderContainers;
import fr.utarwyn.endercontainers.TestHelper;
import fr.utarwyn.endercontainers.TestInitializationException;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class LocaleTest {

    @BeforeClass
    public static void setupClass() throws TestInitializationException {
        TestHelper.setUpFiles();
    }

    @Test
    public void unknownLocaleFile() throws TestInitializationException {
        try {
            new Locale(TestHelper.getPlugin(), "unknown");
            fail("should not load an unknown resource locale file");
        } catch (ConfigLoadingException e) {
            assertThat(e.getMessage()).contains("unknown.yml");
        }
    }

    @Test
    public void saveCustomLocaleFile() throws TestInitializationException, ConfigLoadingException {
        EnderContainers plugin = TestHelper.getPlugin();
        File destination = new File(plugin.getDataFolder(), "locale.yml");

        destination.delete();
        new Locale(plugin, "custom");
        assertThat(destination).exists();
    }

    @Test
    public void getMessage() {
        // Existing key in the file
        assertThat(Files.getLocale().getMessage(LocaleKey.MENU_MAIN_TITLE))
                .isNotNull()
                .isNotEmpty()
                .isEqualTo("Enderchests of %player%");

        // Undefined key in the file
        LocaleKey localeKey = mock(LocaleKey.class);
        when(localeKey.getKey()).thenReturn("UNKNOWN_KEY");
        assertThat(Files.getLocale().getMessage(localeKey)).isNull();
    }

}

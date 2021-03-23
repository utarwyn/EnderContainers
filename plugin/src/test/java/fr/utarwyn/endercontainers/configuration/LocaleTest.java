package fr.utarwyn.endercontainers.configuration;

import fr.utarwyn.endercontainers.TestHelper;
import fr.utarwyn.endercontainers.TestInitializationException;
import fr.utarwyn.endercontainers.configuration.wrapper.YamlFileLoadException;
import org.bukkit.ChatColor;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

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
    public void load() {
        try {
            Files.getLocale().load();
        } catch (YamlFileLoadException e) {
            fail("locale loading should not thrown an exception", e);
        }
    }

    @Test
    public void getMessage() {
        // Existing key in the file
        assertThat(Files.getLocale().getMessage(LocaleKey.MENU_MAIN_TITLE))
                .isNotNull()
                .isNotEmpty()
                .isEqualTo(ChatColor.GOLD + "CHEST MAIN TITLE");

        // Undefined key in the file
        LocaleKey localeKey = mock(LocaleKey.class);
        when(localeKey.getKey()).thenReturn("UNKNOWN_KEY");
        assertThat(Files.getLocale().getMessage(localeKey)).isNull();
    }

}

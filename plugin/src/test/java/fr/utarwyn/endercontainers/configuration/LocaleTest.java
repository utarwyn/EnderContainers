package fr.utarwyn.endercontainers.configuration;

import fr.utarwyn.endercontainers.EnderContainers;
import fr.utarwyn.endercontainers.TestHelper;
import fr.utarwyn.endercontainers.TestInitializationException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LocaleTest {

    @BeforeAll
    static void setupClass() throws TestInitializationException {
        TestHelper.setUpFiles();
    }

    @Test
    void unknownLocaleFile() throws TestInitializationException {
        try {
            new Locale(TestHelper.getPlugin(), "unknown");
            fail("should not load an unknown resource locale file");
        } catch (ConfigLoadingException e) {
            assertThat(e.getMessage()).contains("unknown.yml");
        }
    }

    @Test
    void saveCustomLocaleFile() throws TestInitializationException, ConfigLoadingException {
        EnderContainers plugin = TestHelper.getPlugin();
        File destination = new File(plugin.getDataFolder(), "locale.yml");

        destination.delete();
        new Locale(plugin, "custom");
        assertThat(destination).exists();
    }

    @Test
    void getMessage() {
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

    @Test
    void replaceWithMessages() {
        // No locale key in input test
        assertThat(Files.getLocale().replaceWithMessages("simple text without locale key"))
                .isEqualTo("simple text without locale key");

        // With multiple locale keys
        assertThat(Files.getLocale().replaceWithMessages("({{menus.previous_page}},{{menus.next_page}})"))
                .isEqualTo("(§c≪ Previous page,§cNext page ≫)");
    }

}

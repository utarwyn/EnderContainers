package fr.utarwyn.endercontainers.configuration;

import fr.utarwyn.endercontainers.TestHelper;
import org.bukkit.ChatColor;
import org.bukkit.configuration.InvalidConfigurationException;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class LocaleTest {

    @BeforeClass
    public static void setupClass() throws InvalidConfigurationException, ReflectiveOperationException, IOException {
        TestHelper.setUpFiles();
    }

    @Test
    public void load() {
        assertThat(Files.getLocale().load()).isTrue();
    }

    @Test
    public void getMessage() {
        // Existing key in the file
        assertThat(Files.getLocale().getMessage(LocaleKey.MENU_MAIN_TITLE))
                .isNotNull()
                .isNotEmpty()
                .isEqualTo(ChatColor.GOLD + "CHEST MAIN TITLE");

        // Undefined key in the file
        assertThat(Files.getLocale().getMessage(LocaleKey.MENU_CHEST_LOCKED))
                .isNull();
    }

    @Test
    public void parseValue() {
        // Parse a random value
        assertThat(Files.getLocale().parseValue("key", 50))
                .isNotNull()
                .isEqualTo(50);

        // Parse a string with a random color
        assertThat(Files.getLocale().parseValue("key", "&6test"))
                .isNotNull()
                .isEqualTo("§6test");
    }

}

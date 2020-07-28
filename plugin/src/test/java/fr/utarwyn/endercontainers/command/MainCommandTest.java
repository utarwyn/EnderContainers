package fr.utarwyn.endercontainers.command;

import fr.utarwyn.endercontainers.TestHelper;
import fr.utarwyn.endercontainers.backup.BackupManager;
import fr.utarwyn.endercontainers.enderchest.EnderChestManager;
import fr.utarwyn.endercontainers.util.Updater;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class MainCommandTest {

    private MainCommand command;

    @Mock
    private Plugin plugin;

    @Mock
    private Player player;

    @Before
    public void setUp() throws ReflectiveOperationException {
        TestHelper.registerManagers(new EnderChestManager(), new BackupManager(), new Updater());

        PluginDescriptionFile descriptionFile = mock(PluginDescriptionFile.class);
        when(descriptionFile.getAuthors()).thenReturn(Collections.singletonList("Utarwyn"));
        when(descriptionFile.getVersion()).thenReturn("2.0.0");
        when(this.plugin.getDescription()).thenReturn(descriptionFile);

        this.command = new MainCommand(this.plugin);
    }

    @Test
    public void create() {
        assertThat(this.command.getName()).isEqualTo("endercontainers");
        assertThat(this.command.getAliases()).containsExactly("ecp");
    }

    @Test
    public void perform() {
        this.command.perform(this.player);
        // Verify that basic informations are sent to the player
        verify(this.player).sendMessage(contains("Utarwyn"));
        verify(this.player).sendMessage(contains("2.0.0"));
        verify(this.player).sendMessage(contains("/ecp help"));
    }

}

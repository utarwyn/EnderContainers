package fr.utarwyn.endercontainers.command;

import fr.utarwyn.endercontainers.TestHelper;
import fr.utarwyn.endercontainers.TestInitializationException;
import fr.utarwyn.endercontainers.backup.BackupManager;
import fr.utarwyn.endercontainers.enderchest.EnderChestManager;
import fr.utarwyn.endercontainers.util.Updater;
import org.bukkit.entity.Player;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.contains;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class MainCommandTest {

    private MainCommand command;

    @Mock
    private Player player;

    @Before
    public void setUp() throws TestInitializationException {
        TestHelper.registerManagers(new EnderChestManager(), new BackupManager(), new Updater());
        this.command = new MainCommand(TestHelper.getPlugin());
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

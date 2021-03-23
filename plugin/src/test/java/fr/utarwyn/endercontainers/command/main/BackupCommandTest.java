package fr.utarwyn.endercontainers.command.main;

import fr.utarwyn.endercontainers.TestHelper;
import fr.utarwyn.endercontainers.TestInitializationException;
import fr.utarwyn.endercontainers.backup.BackupManager;
import fr.utarwyn.endercontainers.command.CommandTestHelper;
import org.bukkit.entity.Player;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class BackupCommandTest extends CommandTestHelper<BackupCommand> {

    @Mock
    private BackupManager manager;

    @Mock
    private Player player;

    @BeforeClass
    public static void setUpClass() throws TestInitializationException {
        TestHelper.setUpFiles();
    }

    @Before
    public void setUp() throws TestInitializationException {
        TestHelper.registerManagers(this.manager);
        this.command = new BackupCommand();
    }

    @Test
    public void create() {
        assertThat(this.command.getName()).isEqualTo("backup");
    }

    @Test
    public void withPermission() {
        when(this.player.hasPermission("endercontainers.backup.list")).thenReturn(true);
        when(this.player.hasPermission("endercontainers.backup.remove")).thenReturn(true);

        this.run(this.player);
        verify(this.player).sendMessage(contains("EnderContainers"));
        verify(this.player).sendMessage(contains("§6/ecp backup list"));
        verify(this.player).sendMessage(contains("§6/ecp backup remove"));
    }

    @Test
    public void withoutPermission() {
        this.run(this.player);
        verify(this.player).sendMessage(contains("§m/ecp backup list"));
        verify(this.player).sendMessage(contains("§m/ecp backup remove"));
    }

    @Test
    public void runSubCommand() {
        when(this.player.hasPermission("endercontainers.backup.list")).thenReturn(true);
        this.run(this.player, "list");
        verify(this.player).sendMessage(contains("No backup"));
    }

}

package fr.utarwyn.endercontainers.command.backup;

import fr.utarwyn.endercontainers.TestHelper;
import fr.utarwyn.endercontainers.TestInitializationException;
import fr.utarwyn.endercontainers.backup.Backup;
import fr.utarwyn.endercontainers.backup.BackupManager;
import org.bukkit.entity.Player;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.sql.Timestamp;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class InfoCommandTest {

    @Mock
    private BackupManager backupManager;

    @Mock
    private Player player;

    @Before
    public void setUp() throws TestInitializationException {
        TestHelper.setUpFiles();
    }

    @Test
    public void create() {
        InfoCommand command = new InfoCommand(this.backupManager);
        assertThat(command.manager).isNotNull().isEqualTo(this.backupManager);
        assertThat(command.getName()).isEqualTo("info");
    }

    @Test
    public void noPermission() {
        InfoCommand command = spy(new InfoCommand(this.backupManager));
        command.onCommand(this.player, command, null, new String[]{"noperm"});
        verify(command, never()).perform(this.player);
    }

    @Test
    public void performWithNoBackup() {
        InfoCommand command = new InfoCommand(this.backupManager);

        when(player.hasPermission(anyString())).thenReturn(true);

        command.onCommand(this.player, command, null, new String[]{"unknown"});

        verify(this.backupManager).getBackupByName("unknown");
        verify(this.player).sendMessage(contains("unknown"));
    }

    @Test
    public void perform() {
        InfoCommand command = new InfoCommand(this.backupManager);
        Optional<Backup> backup = Optional.of(new Backup("backupname",
                new Timestamp(System.currentTimeMillis()), "Utarwyn"));

        when(player.hasPermission(anyString())).thenReturn(true);
        when(this.backupManager.getBackupByName("backup")).thenReturn(backup);

        // Perform the backup info command
        command.onCommand(this.player, command, null, new String[]{"backup"});

        verify(player).sendMessage(contains("/ecp backup load"));
        verify(player).sendMessage(contains("/ecp backup remove"));
        verify(player).sendMessage(contains("backupname"));
        verify(player).sendMessage(contains("Utarwyn"));
    }

}

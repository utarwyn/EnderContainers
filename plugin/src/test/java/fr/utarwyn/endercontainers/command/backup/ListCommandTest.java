package fr.utarwyn.endercontainers.command.backup;

import fr.utarwyn.endercontainers.TestHelper;
import fr.utarwyn.endercontainers.TestInitializationException;
import fr.utarwyn.endercontainers.backup.Backup;
import fr.utarwyn.endercontainers.backup.BackupManager;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ListCommandTest {

    @Mock
    private BackupManager backupManager;

    @Mock
    private Player player;

    @BeforeEach
    public void setUp() throws TestInitializationException {
        TestHelper.setUpFiles();
    }

    @Test
    public void create() {
        ListCommand command = new ListCommand(this.backupManager);
        assertThat(command.manager).isNotNull().isEqualTo(this.backupManager);
        assertThat(command.getName()).isEqualTo("list");
    }

    @Test
    public void noPermission() {
        ListCommand command = spy(new ListCommand(this.backupManager));
        command.onCommand(this.player, command, null, new String[]{"noperm"});
        verify(command, never()).perform(this.player);
    }

    @Test
    public void performWithNoBackup() {
        ListCommand command = new ListCommand(this.backupManager);

        when(player.hasPermission(anyString())).thenReturn(true);

        command.onCommand(this.player, command, null, new String[0]);

        verify(this.backupManager).getBackups();
        verify(this.player).sendMessage(contains("No backup found"));
    }

    @Test
    public void perform() {
        ListCommand command = new ListCommand(this.backupManager);
        List<Backup> backups = Arrays.asList(
                new Backup("backup1", null, "user1"),
                new Backup("backup2", null, "user2")
        );

        when(player.hasPermission(anyString())).thenReturn(true);
        when(this.backupManager.getBackups()).thenReturn(backups);

        // Perform the backup info command
        command.onCommand(this.player, command, null, new String[0]);

        verify(player).sendMessage(contains("backup1"));
        verify(player).sendMessage(contains("backup2"));
        verify(player).sendMessage(contains("Backup list"));
    }

}

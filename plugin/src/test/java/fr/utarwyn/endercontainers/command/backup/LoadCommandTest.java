package fr.utarwyn.endercontainers.command.backup;

import fr.utarwyn.endercontainers.TestHelper;
import fr.utarwyn.endercontainers.TestInitializationException;
import fr.utarwyn.endercontainers.backup.BackupManager;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class LoadCommandTest {

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
        LoadCommand command = new LoadCommand(this.backupManager);
        assertThat(command.manager).isNotNull().isEqualTo(this.backupManager);
        assertThat(command.getName()).isEqualTo("load");
    }

    @Test
    public void noPermission() {
        LoadCommand command = spy(new LoadCommand(this.backupManager));
        command.onCommand(this.player, command, null, new String[]{"noperm"});
        verify(command, never()).perform(this.player);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void perform() {
        ArgumentCaptor<Consumer<Boolean>> consumer = ArgumentCaptor.forClass(Consumer.class);
        LoadCommand command = new LoadCommand(this.backupManager);

        when(player.hasPermission(anyString())).thenReturn(true);

        // Perform the backup creation command
        command.onCommand(this.player, command, null, new String[]{"testbackup"});
        verify(this.backupManager).applyBackup(eq("testbackup"), consumer.capture());

        // Check message sent to the player after creation
        consumer.getValue().accept(true);
        verify(player).sendMessage(contains("has been loaded"));
        consumer.getValue().accept(false);
        verify(player).sendMessage(contains("not found"));
    }

}

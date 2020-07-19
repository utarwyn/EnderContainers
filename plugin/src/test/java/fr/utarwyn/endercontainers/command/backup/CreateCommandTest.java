package fr.utarwyn.endercontainers.command.backup;

import fr.utarwyn.endercontainers.TestHelper;
import fr.utarwyn.endercontainers.backup.BackupManager;
import fr.utarwyn.endercontainers.configuration.wrapper.YamlFileLoadException;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.Player;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class CreateCommandTest {

    @Mock
    private BackupManager backupManager;

    @Mock
    private Player player;

    @Before
    public void setUp() throws ReflectiveOperationException, YamlFileLoadException,
            InvalidConfigurationException, IOException {
        TestHelper.setUpFiles();
    }

    @Test
    public void create() {
        CreateCommand command = new CreateCommand(this.backupManager);
        assertThat(command.manager).isNotNull().isEqualTo(this.backupManager);
        assertThat(command.getName()).isEqualTo("create");
    }

    @Test
    public void noPermission() {
        CreateCommand command = spy(new CreateCommand(this.backupManager));
        command.onCommand(this.player, command, null, new String[]{"noperm"});
        verify(command, never()).perform(this.player);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void perform() {
        ArgumentCaptor<Consumer<Boolean>> consumer = ArgumentCaptor.forClass(Consumer.class);
        CreateCommand command = new CreateCommand(this.backupManager);

        when(player.hasPermission(anyString())).thenReturn(true);
        when(player.getName()).thenReturn("Utarwyn");

        // Perform the backup creation command
        command.onCommand(this.player, command, null, new String[]{"testbackup"});
        verify(this.backupManager).createBackup(eq("testbackup"), eq("Utarwyn"), consumer.capture());

        // Check message sent to the player after creation
        consumer.getValue().accept(true);
        verify(player).sendMessage(contains("created"));
        consumer.getValue().accept(false);
        verify(player).sendMessage(contains("exists"));
    }

}

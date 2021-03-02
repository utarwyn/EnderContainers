package fr.utarwyn.endercontainers.command;

import fr.utarwyn.endercontainers.TestHelper;
import fr.utarwyn.endercontainers.configuration.wrapper.YamlFileLoadException;
import fr.utarwyn.endercontainers.enderchest.EnderChestManager;
import fr.utarwyn.endercontainers.enderchest.context.PlayerContext;
import org.bukkit.ChatColor;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.Player;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class EnderchestCommandTest {

    private EnderchestCommand command;

    @Mock
    private EnderChestManager manager;

    @Mock
    private PlayerContext context;

    private Player player;

    @BeforeClass
    public static void setUpClass() throws ReflectiveOperationException, YamlFileLoadException,
            InvalidConfigurationException, IOException {
        TestHelper.setUpFiles();
    }

    @Before
    public void setUp() throws ReflectiveOperationException {
        TestHelper.registerManagers(this.manager);
        this.player = TestHelper.getPlayer();
        this.command = new EnderchestCommand();

        doAnswer(answer -> {
            ((Consumer<PlayerContext>) answer.getArgument(1)).accept(this.context);
            return null;
        }).when(this.manager).loadPlayerContext(any(), any());
    }

    @Test
    public void create() {
        assertThat(this.command.getName()).isEqualTo("enderchest");
        assertThat(this.command.getAliases()).containsExactly("ec", "endchest");
    }

    @Test
    public void disableInConsole() {
        ConsoleCommandSender sender = mock(ConsoleCommandSender.class);
        this.command.onCommand(sender, this.command, null, new String[0]);
        // we should receive an error message (containing red color)
        verify(sender).sendMessage(contains(ChatColor.RED.toString()));
    }

    @Test
    public void disabledWorld() {
        when(this.player.getWorld().getName()).thenReturn("disabled");
        this.command.onCommand(this.player, this.command, null, new String[0]);
        verify(this.player).sendMessage(contains("disabled"));
        verify(this.manager, never()).loadPlayerContext(any(), any());
    }

    @Test
    public void openMainChest() {
        when(this.player.hasPermission("endercontainers.cmd.enderchests")).thenReturn(true);
        this.command.onCommand(this.player, this.command, null, new String[0]);
        verify(this.context).openHubMenuFor(this.player);
    }

    @Test
    public void openSpecificChest() {
        // With global permission
        when(this.player.hasPermission("endercontainers.cmd.enderchests")).thenReturn(true);
        this.command.onCommand(this.player, this.command, null, new String[]{"10"});
        verify(this.context).openEnderchestFor(this.player, 9);

        // With chest specific permission
        when(this.player.hasPermission("endercontainers.cmd.enderchests")).thenReturn(false);
        when(this.player.hasPermission("endercontainers.cmd.enderchest.4")).thenReturn(true);
        this.command.onCommand(this.player, this.command, null, new String[]{"5"});
        verify(this.context).openEnderchestFor(this.player, 4);
    }

    @Test
    public void noPermission() {
        // No permission for all chests
        this.command.onCommand(this.player, this.command, null, new String[0]);

        // No permission for specific chest
        this.command.onCommand(this.player, this.command, null, new String[]{"5"});

        verify(this.player).hasPermission("endercontainers.cmd.enderchest.4");
        verify(this.player, times(2)).sendMessage(contains("perm"));
    }

    @Test
    public void errorEnderchestNumber() {
        this.command.onCommand(this.player, this.command, null, new String[]{"500"});
        verify(this.player).sendMessage(contains("perm"));
    }

    @Test
    public void errorEnderchestNotAccessible() {
        when(this.player.hasPermission("endercontainers.cmd.enderchests")).thenReturn(true);
        when(this.context.openEnderchestFor(this.player, 4)).thenReturn(false);
        this.command.onCommand(this.player, this.command, null, new String[]{"5"});
        verify(this.player).sendMessage(contains("open"));
    }

}

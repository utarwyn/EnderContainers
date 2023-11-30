package fr.utarwyn.endercontainers.command;

import fr.utarwyn.endercontainers.TestHelper;
import fr.utarwyn.endercontainers.TestInitializationException;
import fr.utarwyn.endercontainers.enderchest.EnderChestManager;
import fr.utarwyn.endercontainers.enderchest.context.PlayerContext;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EnderchestCommandTest extends CommandTestHelper<EnderchestCommand> {

    @Mock
    private EnderChestManager manager;

    @Mock
    private PlayerContext context;

    private Player player;

    @BeforeAll
    public static void setUpClass() throws TestInitializationException {
        TestHelper.setUpFiles();
    }

    @BeforeEach
    public void setUp() throws TestInitializationException {
        TestHelper.registerManagers(this.manager);
        this.player = TestHelper.getPlayer();
        this.command = new EnderchestCommand();
        this.permission = "endercontainers.cmd.enderchests";

        lenient().doAnswer(answer -> {
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
        this.run(sender);
        verify(sender).sendMessage(contains("player"));
    }

    @Test
    public void disabledWorld() {
        when(this.player.getWorld().getName()).thenReturn("disabled");
        this.run(this.player);
        verify(this.player).sendMessage(contains("disabled"));
        verify(this.manager, never()).loadPlayerContext(any(), any());
    }

    @Test
    public void openChestListInventory() {
        this.givePermission(this.player);
        this.run(this.player);
        verify(this.context).openListInventory(this.player);
    }

    @Test
    public void openSpecificChestInventory() {
        // With global permission
        this.givePermission(this.player);
        this.run(this.player, "10");
        verify(this.context).openEnderchestInventory(this.player, 9);

        // With chest specific permission
        this.setPermissionState(this.player, false);
        when(this.player.hasPermission("endercontainers.cmd.enderchest.4")).thenReturn(true);
        this.run(this.player, "5");
        verify(this.context).openEnderchestInventory(this.player, 4);
    }

    @Test
    public void noPermission() {
        // No permission for all chests
        this.run(this.player);

        // No permission for specific chest
        this.run(this.player, "5");

        verify(this.player).hasPermission("endercontainers.cmd.enderchest.4");
        this.verifyNoPerm(this.player, 2);
    }

    @Test
    public void errorEnderchestNumber() {
        this.givePermission(this.player);
        this.run(this.player, "-20");
        this.run(this.player, "0");
        this.run(this.player, "500");
        this.verifyNoPerm(this.player, 3);

        this.run(this.player, "ezaeza");
        verify(this.player).sendMessage(contains("not valid"));
    }

    @Test
    public void errorEnderchestNotAccessible() {
        this.givePermission(this.player);
        when(this.context.openEnderchestInventory(this.player, 4)).thenReturn(false);
        this.run(this.player, "5");
        verify(this.player).sendMessage(contains("open"));
    }

}

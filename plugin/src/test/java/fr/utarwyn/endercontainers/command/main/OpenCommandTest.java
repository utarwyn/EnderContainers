package fr.utarwyn.endercontainers.command.main;

import fr.utarwyn.endercontainers.TestHelper;
import fr.utarwyn.endercontainers.TestInitializationException;
import fr.utarwyn.endercontainers.command.CommandTestHelper;
import fr.utarwyn.endercontainers.enderchest.EnderChestManager;
import fr.utarwyn.endercontainers.enderchest.context.PlayerContext;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class OpenCommandTest extends CommandTestHelper<OpenCommand> {

    @Mock
    private EnderChestManager manager;

    @Mock
    private PlayerContext context;

    private Player player;

    @BeforeClass
    public static void setUpClass() throws TestInitializationException {
        TestHelper.setUpFiles();
    }

    @Before
    public void setUp() throws TestInitializationException {
        TestHelper.registerManagers(this.manager);
        this.player = TestHelper.getPlayer();
        this.command = new OpenCommand();
        this.permission = "endercontainers.openchests";

        doAnswer(answer -> {
            ((Consumer<PlayerContext>) answer.getArgument(1)).accept(this.context);
            return null;
        }).when(this.manager).loadPlayerContext(any(), any());
    }

    @Test
    public void create() {
        assertThat(this.command.getName()).isEqualTo("open");
    }

    @Test
    public void disableInConsole() {
        ConsoleCommandSender sender = mock(ConsoleCommandSender.class);
        this.run(sender, "test");
        verify(sender).sendMessage(contains("player"));
    }

    @Test
    public void disabledWorld() {
        this.givePermission(this.player);
        when(this.player.getWorld().getName()).thenReturn("disabled");

        this.run(this.player, "test");

        verify(this.player).sendMessage(contains("disabled"));
        verify(this.manager, never()).loadPlayerContext(any(), any());
    }

    @Test
    public void openMainChest() {
        this.givePermission(this.player);
        this.run(this.player, this.player.getName());
        verify(this.context).openListInventory(this.player);
    }

    @Test
    public void tabCompletion() {
        // No completions without permission
        assertThat(this.tabComplete(this.player, "")).isEmpty();

        // Only current player is available
        this.givePermission(this.player);
        assertThat(this.tabComplete(this.player, "")).containsExactly("Utarwyn");
        assertThat(this.tabComplete(this.player, "Uta")).containsExactly("Utarwyn");
        assertThat(this.tabComplete(this.player, "nyw")).isEmpty();
    }

    @Test
    public void noPermission() {
        this.run(this.player, "test");
        this.verifyNoPerm(this.player);
    }

    @Test
    public void errorArgumentCount() {
        this.run(this.player);
        verify(this.player).sendMessage(contains("argument count"));
    }

    @Test
    public void errorPlayerNotFound() {
        this.givePermission(this.player);
        this.run(this.player, "unknown");
        verify(this.player).sendMessage(contains("not found"));
    }

}

package fr.utarwyn.endercontainers.command.main;

import fr.utarwyn.endercontainers.command.CommandTestHelper;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class HelpCommandTest extends CommandTestHelper<HelpCommand> {

    @Mock
    private Player player;

    @BeforeEach
    public void setUp() {
        this.command = new HelpCommand();
    }

    @Test
    public void create() {
        assertThat(this.command.getName()).isEqualTo("help");
        assertThat(this.command.getAliases()).containsExactly("h", "?");
    }

    @Test
    public void withPermission() {
        when(this.player.hasPermission(anyString())).thenReturn(false);
        when(this.player.hasPermission("endercontainers.reload")).thenReturn(true);
        when(this.player.hasPermission("endercontainers.update")).thenReturn(true);

        this.run(this.player);
        verify(this.player).sendMessage(contains("EnderContainers"));
        verify(this.player).sendMessage(contains("§3/ecp update"));
        verify(this.player).sendMessage(contains("§3/ecp reload"));
    }

    @Test
    public void withoutPermission() {
        this.run(this.player);
        verify(this.player).sendMessage(contains("§m/ecp update"));
        verify(this.player).sendMessage(contains("§m/ecp reload"));
    }

}

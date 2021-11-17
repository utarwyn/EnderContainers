package fr.utarwyn.endercontainers.util;

import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class MiscUtilTest {

    @Test
    public void playerHasPerm() {
        Player player = mock(Player.class);
        final String perm = "update";

        assertThat(MiscUtil.playerHasPerm(player, perm)).isFalse();

        when(player.isOp()).thenReturn(true);
        assertThat(MiscUtil.playerHasPerm(player, perm)).isTrue();

        when(player.isOp()).thenReturn(false);
        when(player.hasPermission("endercontainers." + perm)).thenReturn(true);
        assertThat(MiscUtil.playerHasPerm(player, perm)).isTrue();
    }

    @Test
    public void senderHasPerm() {
        CommandSender player = mock(Player.class);
        CommandSender console = mock(ConsoleCommandSender.class);
        final String perm = "update";

        assertThat(MiscUtil.senderHasPerm(console, perm)).isTrue();
        assertThat(MiscUtil.senderHasPerm(player, perm)).isFalse();

        verify(player).isOp();
    }

}

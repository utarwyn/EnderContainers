package fr.utarwyn.endercontainers.util;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class MiscUtilTest {

    private static final String UNKNOWN_SOUND = "XXXX";

    private static final String EXISITING_SOUND = "AMBIENT_CAVE";

    @Test
    public void playSoundLocation() {
        World world = mock(World.class);
        Location location = mock(Location.class);
        Sound sound = Sound.valueOf(EXISITING_SOUND);

        when(location.getWorld()).thenReturn(world);

        // Unknown sound
        MiscUtil.playSound(location, UNKNOWN_SOUND, UNKNOWN_SOUND);
        verify(world, never()).playSound(location, sound, 1f, 1f);

        // Existing sound
        MiscUtil.playSound(location, EXISITING_SOUND, EXISITING_SOUND);
        verify(world).playSound(location, sound, 1f, 1f);

        // Existing sound in 1.9+
        MiscUtil.playSound(location, UNKNOWN_SOUND, EXISITING_SOUND);
        verify(world, times(2)).playSound(location, sound, 1f, 1f);
    }

    @Test
    public void playSoundPlayer() {
        Player player = mock(Player.class);
        Location location = mock(Location.class);
        Sound sound = Sound.valueOf(EXISITING_SOUND);

        when(player.getLocation()).thenReturn(location);

        // Unknown sound
        MiscUtil.playSound(player, UNKNOWN_SOUND, UNKNOWN_SOUND);
        verify(player, never()).playSound(player.getLocation(), sound, 1f, 1f);

        // First existing sound
        MiscUtil.playSound(player, EXISITING_SOUND);
        verify(player).playSound(player.getLocation(), sound, 1f, 1f);

        // Second existing sound
        MiscUtil.playSound(player, UNKNOWN_SOUND, EXISITING_SOUND);
        verify(player, times(2)).playSound(player.getLocation(), sound, 1f, 1f);
    }

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

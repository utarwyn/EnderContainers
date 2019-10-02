package fr.utarwyn.endercontainers.util;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
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
        verify(world, times(0)).playSound(location, sound, 1f, 1f);

        // Existing sound
        MiscUtil.playSound(location, EXISITING_SOUND, EXISITING_SOUND);
        verify(world, times(1)).playSound(location, sound, 1f, 1f);

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
        verify(player, times(0)).playSound(player.getLocation(), sound, 1f, 1f);

        // Exisiting sound
        MiscUtil.playSound(player, EXISITING_SOUND, EXISITING_SOUND);
        verify(player, times(1)).playSound(player.getLocation(), sound, 1f, 1f);
    }

    @Test
    public void playerHasPerm() {
        Player player = mock(Player.class);
        final String perm = "update";

        assertFalse("A normal player should not have access to plugin perms", MiscUtil.playerHasPerm(player, perm));

        when(player.isOp()).thenReturn(true);
        assertTrue("An OP player must have all plugin perms", MiscUtil.playerHasPerm(player, perm));

        when(player.isOp()).thenReturn(false);
        when(player.hasPermission("endercontainers." + perm)).thenReturn(true);
        assertTrue("A specific perm is not detected by the plugin", MiscUtil.playerHasPerm(player, perm));
    }

    @Test
    public void senderHasPerm() {
        CommandSender player = mock(Player.class);
        CommandSender console = mock(ConsoleCommandSender.class);
        final String perm = "update";

        assertTrue("Console must have all permissions", MiscUtil.senderHasPerm(console, perm));
        assertFalse("Method should manage players", MiscUtil.senderHasPerm(player, perm));

        verify(player, times(1)).isOp();
    }

}

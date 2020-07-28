package fr.utarwyn.endercontainers.util;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.Objects;

/**
 * Miscellaneous utility class of the plugin.
 *
 * @author Utarwyn
 * @since 2.0.0
 */
public class MiscUtil {

    /**
     * It's an utility class. It cannot be instanciated.
     */
    private MiscUtil() {
    }

    /**
     * Plays a sound at a specific location with support of 1.8 sound and 1.9+ sound.
     *
     * @param location  Location where to play the sound
     * @param soundKeys list of sound keys to try for playing the wanted sound
     */
    public static void playSound(Location location, String... soundKeys) {
        Sound sound = MiscUtil.searchSound(soundKeys);
        if (sound == null) return;

        Objects.requireNonNull(location.getWorld())
                .playSound(location, sound, 1f, 1f);
    }

    /**
     * Plays a sound at a specific location with support of 1.8 sound and 1.9+ sound.
     * Plays the sound only for a specific player.
     *
     * @param player    Player which will receive the sound
     * @param soundKeys list of sound keys to try for playing the wanted sound
     */
    public static void playSound(Player player, String... soundKeys) {
        Sound sound = MiscUtil.searchSound(soundKeys);
        if (sound == null) return;

        player.playSound(player.getLocation(), sound, 1f, 1f);
    }

    /**
     * Checks if a player has a specific EnderContainers permission.
     * Permissions are automatically prefixed by the name of the plugin.
     *
     * @param player Player to check
     * @param perm   Permission used for the test
     * @return True if the player has the given permission
     */
    public static boolean playerHasPerm(Player player, String perm) {
        return player.isOp() || player.hasPermission("endercontainers." + perm);
    }

    /**
     * Checks if a command sender has a specific EnderContainers permission.
     * Permissions are automatically prefixed by the name of the plugin.
     *
     * @param sender Command sender to check
     * @param perm   Permission used for the test
     * @return True if the command sender has the given permission
     */
    public static boolean senderHasPerm(CommandSender sender, String perm) {
        return !(sender instanceof Player && !playerHasPerm((Player) sender, perm)) || sender instanceof ConsoleCommandSender;
    }

    /**
     * Searches a sound from its key.
     * This method takes a list of sound names to validate in order to play the sound.
     *
     * @param soundKeys list of sound keys to try for playing the wanted sound
     * @return sound type if found, null otherwise
     */
    private static Sound searchSound(String... soundKeys) {
        for (String soundKey : soundKeys) {
            // Maybe be a little bit trick, but its the fatest way of checking for equality
            try {
                return Sound.valueOf(soundKey);
            } catch (IllegalArgumentException ignored) {
                // Not catched
            }
        }

        return null;
    }

}

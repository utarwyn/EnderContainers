package fr.utarwyn.endercontainers.util;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.io.File;

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
     * @param location Location where to play the sound
     * @param sound18  Sound string for 1.8 versions
     * @param sound19  Sound string for 1.9 versions
     */
    public static void playSound(Location location, String sound18, String sound19) {
        Sound sound = MiscUtil.generateSound(sound18, sound19);
        if (sound == null) return;

        location.getWorld().playSound(location, sound, 1f, 1f);
    }

    /**
     * Plays a sound at a specific location with support of 1.8 sound and 1.9+ sound.
     * Plays the sound only for a specific player.
     *
     * @param player  Player which will receive the sound
     * @param sound18 Sound string for 1.8 versions
     * @param sound19 Sound string for 1.9 versions
     */
    public static void playSound(Player player, String sound18, String sound19) {
        Sound sound = MiscUtil.generateSound(sound18, sound19);
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
     * Returns the existance of a sound by its name
     *
     * @param soundName Sound name to check
     * @return True if the sound with the given name exists
     */
    private static boolean soundExists(String soundName) {
        for (Sound sound : Sound.values())
            if (sound.name().equals(soundName))
                return true;

        return false;
    }

    /**
     * Delete a folder recursively with all files inside of it
     *
     * @param folder Folder to delete
     * @return True if the folder have been dekleted without errors.
     */
    public static boolean deleteFolder(File folder) {
        File[] files = folder.listFiles();
        if (files == null)
            return folder.delete();

        for (File file : files) {
            if (file.isDirectory()) {
                if (!deleteFolder(file))
                    return false;
            } else if (!file.delete())
                return false;
        }

        return folder.delete();
    }

    /**
     * Generates a sound from two string (one for 1.8 and one for 1.9+).
     * The method tries to generate the 1.8 sound, and if its not working it tries to
     * generate the 1.9+ sound, and if its not working too it generate nothing, without error.
     *
     * @param sound18 Sound key for MC 1.8 version.
     * @param sound19 Sound key for MC 1.9+ versions.
     * @return The generated sound, null otherwise.
     */
    private static Sound generateSound(String sound18, String sound19) {
        Sound sound;

        if (MiscUtil.soundExists(sound18))
            sound = Sound.valueOf(sound18);   // 1.8
        else if (MiscUtil.soundExists(sound19))
            sound = Sound.valueOf(sound19);   // 1.9+
        else
            return null;                      // Else? Not supported.

        return sound;
    }

}

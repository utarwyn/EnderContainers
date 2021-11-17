package fr.utarwyn.endercontainers.util;

import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

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

}

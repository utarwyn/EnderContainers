package fr.utarwyn.endercontainers.util;

import fr.utarwyn.endercontainers.EnderContainers;
import fr.utarwyn.endercontainers.configuration.Files;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Utility class used to send various messages for players.
 *
 * @author Utarwyn
 * @since 1.0.0
 */
public class PluginMsg {

    /**
     * Utility class!
     */
    private PluginMsg() {

    }

    /**
     * Send a given error message to a specific sender
     *
     * @param sender  The sender
     * @param message Error message to send
     */
    public static void errorMessage(CommandSender sender, String message) {
        sender.sendMessage(EnderContainers.PREFIX + ChatColor.RED + message);
    }

    /**
     * Send a given error message to a specific sender (simple message without prefix)
     *
     * @param sender  The sender
     * @param message Error message to send
     */
    public static void errorSMessage(CommandSender sender, String message) {
        sender.sendMessage("§c§l(!) §c" + message);
    }

    /**
     * Inform a sender that the access is denied for him
     *
     * @param sender The sender
     */
    public static void accessDenied(CommandSender sender) {
        if (sender instanceof Player) {
            errorMessage(sender, Files.getLocale().getNopermPlayer());
        } else {
            errorMessage(sender, Files.getLocale().getNopermConsole());
        }
    }

    /**
     * Send the plugin header bar to a given sender
     *
     * @param sender Sender to process
     */
    public static void pluginBar(CommandSender sender) {
        String pBar = "§5§m" + StringUtils.repeat("-", 5);
        String sBar = "§d§m" + StringUtils.repeat("-", 11);

        sender.sendMessage("§8++" + pBar + sBar + "§r§d( §6EnderContainers §d)" + sBar + pBar + "§8++");
    }

    /**
     * Send the plugin footer bar to a given sender
     *
     * @param sender Sender to process
     */
    public static void endBar(CommandSender sender) {
        String pBar = "§5§m" + StringUtils.repeat("-", 5);
        sender.sendMessage("§8++" + pBar + "§d§m" + StringUtils.repeat("-", 39) + pBar + "§8++");
    }

}

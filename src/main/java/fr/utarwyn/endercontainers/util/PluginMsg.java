package fr.utarwyn.endercontainers.util;

import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Utility class used to send various messages for players.
 * @since 1.0.0
 * @author Utarwyn
 */
public class PluginMsg {

	/**
	 * Utility class!
	 */
	private PluginMsg() {

	}

	/**
	 * Send a given error message to a specific sender
	 * @param sender The sender
	 * @param message Error message to send
	 */
	public static void errorMessage(CommandSender sender, String message) {
		sender.sendMessage(EUtil.getPrefix() + ChatColor.RED + message);
	}

	/**
	 * Inform a sender that the access is denied for him
	 * @param sender The sender
	 */
	public static void accessDenied(CommandSender sender) {
		errorMessage(sender, LocaleManager.__("error_player_denied"));
	}

	/**
	 * Inform a player that it does not have the perm to do an action
	 * @param p Player to process
	 */
	public static void doesNotHavePerm(Player p) {
		errorMessage(p, "You don't have the permission to do this.");
	}

	/**
	 * Inform a player that it does not have the perm to use a chest in an enemy faction
	 * @param p Player to process
	 */
	public static void cantUseHereFaction(Player p) {
		errorMessage(p, LocaleManager.__("error_access_denied_factions"));
	}

	/**
	 * Inform a player that the plugin is disabled
	 * @param p Player to process
	 */
	public static void pluginDisabled(Player p) {
		errorMessage(p, LocaleManager.__("error_plugin_disabled"));
	}

	/**
	 * Inform a player that the plugin is disabled in the world where he is.
	 * @param p Player to process
	 */
	public static void pluginDisabledInWorld(Player p) {
		errorMessage(p, LocaleManager.__("error_plugin_disabled_world"));
	}

	/**
	 * Send the plugin header bar to a given sender
	 * @param sender Sender to process
	 */
	public static void pluginBar(CommandSender sender) {
		String pBar = "§5§m" + StringUtils.repeat("-", 5);
		String sBar = "§d§m" + StringUtils.repeat("-", 11);

		sender.sendMessage("§8++" + pBar + sBar + "§r§d( §6EnderContainers §d)" + sBar + pBar + "§8++");
	}

	/**
	 * Send the plugin footer bar to a given sender
	 * @param sender Sender to process
	 */
	public static void endBar(CommandSender sender) {
		String pBar = "§5§m" + StringUtils.repeat("-", 5);
		sender.sendMessage("§8++" + pBar + "§d§m" + StringUtils.repeat("-", 39) + pBar + "§8++");
	}

}

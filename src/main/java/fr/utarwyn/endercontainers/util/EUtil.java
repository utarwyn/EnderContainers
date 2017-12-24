package fr.utarwyn.endercontainers.util;

import com.google.common.collect.Maps;
import fr.utarwyn.endercontainers.EnderContainers;
import org.bukkit.*;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.Map;
import java.util.UUID;

/**
 * Main utility class of the plugin.
 * All useful methods are here.
 *
 * @since 2.0.0
 * @author Utarwyn
 */
public class EUtil {

	/**
	 * A map to store relations between dye colors and chat colors
	 */
	private static Map<DyeColor, ChatColor> dyeChatMap;

	/**
	 * It's an utility class. It cannot be instanciated.
	 */
	private EUtil() {  }

	static {
		dyeChatMap = Maps.newHashMap();
		dyeChatMap.put(DyeColor.BLACK, ChatColor.DARK_GRAY);
		dyeChatMap.put(DyeColor.BLUE, ChatColor.DARK_BLUE);
		dyeChatMap.put(DyeColor.BROWN, ChatColor.GOLD);
		dyeChatMap.put(DyeColor.CYAN, ChatColor.AQUA);
		dyeChatMap.put(DyeColor.GRAY, ChatColor.GRAY);
		dyeChatMap.put(DyeColor.GREEN, ChatColor.DARK_GREEN);
		dyeChatMap.put(DyeColor.LIGHT_BLUE, ChatColor.BLUE);
		dyeChatMap.put(DyeColor.LIME, ChatColor.GREEN);
		dyeChatMap.put(DyeColor.MAGENTA, ChatColor.LIGHT_PURPLE);
		dyeChatMap.put(DyeColor.ORANGE, ChatColor.GOLD);
		dyeChatMap.put(DyeColor.PINK, ChatColor.LIGHT_PURPLE);
		dyeChatMap.put(DyeColor.PURPLE, ChatColor.DARK_PURPLE);
		dyeChatMap.put(DyeColor.RED, ChatColor.DARK_RED);
		dyeChatMap.put(DyeColor.SILVER, ChatColor.GRAY);
		dyeChatMap.put(DyeColor.WHITE, ChatColor.WHITE);
		dyeChatMap.put(DyeColor.YELLOW, ChatColor.YELLOW);
	}

	/**
	 * Gets a dye color from a chat color. That's all.
	 * @param chatColor The chat color
	 * @return Dye color related or null
	 */
	public static DyeColor getDyeColorFromChatColor(ChatColor chatColor) {
		for (Map.Entry<DyeColor, ChatColor> entry : dyeChatMap.entrySet())
			if (entry.getValue().equals(chatColor))
				return entry.getKey();

		return null;
	}

	/**
	 * Gets the content size of an inventory (filled slots)
	 * @param inv Inventory to analyze
	 * @return Number of filled slots in an inventory
	 */
	public static int getInventorySize(Inventory inv) {
		int r = 0;

		for (ItemStack i : inv.getContents())
			if (i != null)
				r++;

		return r;
	}

	/**
	 * Plays a sound at a specific location with support of 1.8 sound and 1.9+ sound.
	 * The method tries to play the 1.8 sound, and if its not working it tries to
	 * play the 1.9+ sound, and if its not working too it plays nothing, whithout error.
	 *
	 * @param location Location where to play the sound
	 * @param sound18 Sound string for 1.8 versions
	 * @param sound19 Sound string for 1.9 versions
	 */
	public static void playSound(Location location, String sound18, String sound19) {
		Sound sound;

		if (EUtil.soundExists(sound18))
			sound = Sound.valueOf(sound18);   // 1.8
		else if (EUtil.soundExists(sound19))
			sound = Sound.valueOf(sound19);   // 1.9+
		else
			return;                           // Else? Not supported.

		location.getWorld().playSound(location, sound, 1f, 1f);
	}

	/**
	 * Gets the vanilla enderchest for an offline player, using NMS hacks.
	 * @param owner UUID of the owner of the chest
	 * @return The inventory which represents the enderchest
	 */
	public static Inventory getVanillaEnderchestOf(UUID owner) {
		Player player = Bukkit.getPlayer(owner);
		// Player is online
		if (player != null && player.isOnline())
			return player.getEnderChest();

		Player offlinePlayer = NMSHacks.getPlayerObjectOfOfflinePlayer("", owner);

		if (offlinePlayer != null) {
			offlinePlayer.loadData();
			return offlinePlayer.getEnderChest();
		}

		return null;
	}

	/**
	 * Saves the vanilla enderchest of an offline player, using NMS hacks.
	 * @param owner UUID of the owner of the chest
	 * @param inv The inventory which have to be saved as the enderchest of the player
	 */
	public static void saveVanillaEnderchestOf(UUID owner, Inventory inv) {
		Player offlinePlayer = NMSHacks.getPlayerObjectOfOfflinePlayer("", owner);

		if (offlinePlayer != null) {
			offlinePlayer.loadData();
			offlinePlayer.getEnderChest().setContents(inv.getContents());
			offlinePlayer.saveData();
		}
	}

	/**
	 * Returns if a player with the given uuid is online or not
	 * @param uuid The UUID of the player to check
	 * @return True if the player is online
	 */
	public static boolean isPlayerOnline(UUID uuid) {
		Player player = Bukkit.getPlayer(uuid);
		return player != null && player.isOnline();
	}

	/**
	 * Checks if a player has a specific EnderContainers permission.
	 * Permissions are automatically prefixed by the name of the plugin.
	 *
	 * @param player Player to check
	 * @param perm Permission used for the test
	 * @return True if the player has the given permission
	 */
	public static boolean playerHasPerm(Player player, String perm) {
		return player.hasPermission("endercontainers." + perm);
	}

	/**
	 * Checks if a command sender has a specific EnderContainers permission.
	 * Permissions are automatically prefixed by the name of the plugin.
	 *
	 * @param sender Command sender to check
	 * @param perm Permission used for the test
	 * @return True if the command sender has the given permission
	 */
	public static boolean senderHasPerm(CommandSender sender, String perm) {
		return !(sender instanceof Player && !playerHasPerm((Player) sender, perm)) || sender instanceof ConsoleCommandSender;
	}

	/**
	 * Gets the formatted server version
	 * @return The server version as a string
	 */
	public static String getServerVersion() {
		String packageName = EnderContainers.getInstance().getServer().getClass().getPackage().getName();
		return packageName.substring(packageName.lastIndexOf('.') + 1);
	}

	/**
	 * Shortcut used to create an asynchronous Java thread
	 * @param runnable Runnable to run asynchronously
	 */
	public static void runAsync(Runnable runnable) {
		Bukkit.getScheduler().runTaskAsynchronously(EnderContainers.getInstance(), runnable);
	}

	/**
	 * Shortcut used to create an synchronous Java thread
	 * @param runnable Runnable to run synchronously
	 */
	public static void runSync(Runnable runnable) {
		Bukkit.getScheduler().runTask(EnderContainers.getInstance(), runnable);
	}

	/**
	 * Returns the existance of a sound by its name
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
			} else
				if (!file.delete())
					return false;
		}

		return folder.delete();
	}

}

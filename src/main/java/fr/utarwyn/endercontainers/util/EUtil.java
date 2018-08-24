package fr.utarwyn.endercontainers.util;

import fr.utarwyn.endercontainers.EnderContainers;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.io.File;
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
	 * It's an utility class. It cannot be instanciated.
	 */
	private EUtil() {  }

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
	 * Method to clamp a value
	 * @param val Value
	 * @param min Minimum
	 * @param max Maximum
	 * @return The clamped value
	 */
	public static int clamp(int val, int min, int max) {
		return Math.max(min, Math.min(max, val));
	}

	/**
	 * Plays a sound at a specific location with support of 1.8 sound and 1.9+ sound.
	 * @param location Location where to play the sound
	 * @param sound18 Sound string for 1.8 versions
	 * @param sound19 Sound string for 1.9 versions
	 */
	public static void playSound(Location location, String sound18, String sound19) {
		Sound sound = EUtil.generateSound(sound18, sound19);
		if (sound == null) return;

		location.getWorld().playSound(location, sound, 1f, 1f);
	}

	/**
	 * Plays a sound at a specific location with support of 1.8 sound and 1.9+ sound.
	 * Plays the sound only for a specific player.
	 *
	 * @param player Player which will receive the sound
	 * @param sound18 Sound string for 1.8 versions
	 * @param sound19 Sound string for 1.9 versions
	 */
	public static void playSound(Player player, String sound18, String sound19) {
		Sound sound = EUtil.generateSound(sound18, sound19);
		if (sound == null) return;

		player.playSound(player.getLocation(), sound, 1f, 1f);
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
		return player.isOp() || player.hasPermission("endercontainers." + perm);
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

		if (EUtil.soundExists(sound18))
			sound = Sound.valueOf(sound18);   // 1.8
		else if (EUtil.soundExists(sound19))
			sound = Sound.valueOf(sound19);   // 1.9+
		else
			return null;                      // Else? Not supported.

		return sound;
	}

}

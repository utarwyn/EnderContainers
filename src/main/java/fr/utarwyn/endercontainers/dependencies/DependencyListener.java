package fr.utarwyn.endercontainers.dependencies;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;

/**
 * Class which introduce all methods used to listen for a EnderContainers event
 * (called for each registered dependency)
 * @since 2.0.0
 * @author Utarwyn
 */
public interface DependencyListener {

	/**
	 * Called when a player wants to open its enderchest by interacting with an enderchest block
	 * @param block The block clicked by the player
	 * @param player The player who interacts with the chest.
	 * @param sendMessage The plugin have to send message to the player?
	 * @return True if the block chest can be opened
	 */
	boolean onBlockChestOpened(Block block, Player player, boolean sendMessage);

}

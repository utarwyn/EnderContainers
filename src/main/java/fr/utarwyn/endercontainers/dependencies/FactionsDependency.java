package fr.utarwyn.endercontainers.dependencies;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

/**
 * Dependency used to interact with the Factions plugin
 * @since 1.0.3
 * @author Utarwyn
 */
public class FactionsDependency extends Dependency {

	private FactionsHook factionHook;

	/**
	 * Construct the dependency object
	 */
	FactionsDependency() {
		super("Factions");
	}

	/**
	 * Called when the dependency is enabling
	 */
	@Override
	public void onEnable() {
		Plugin factions = Bukkit.getPluginManager().getPlugin("Factions");
		// No plugin Factions detected?
		if (factions == null) return;

		// Get Factions version
		String[] components = factions.getDescription().getVersion().split("\\.");
		String version = components.length < 2 ? "" : components[0] + "." + components[1];

		// Instanciate the correct hook in terms of the version of Factions
		switch (version) {
			case "1.6": // Old version of Factions - FactionsUUID - SavageFactions
				this.factionHook = new Factions0106Dependency();
				break;
			default:    // New versions of Factions!
				this.factionHook = new Factions0212Dependency();
		}
	}

	/**
	 * Called when the dependency is disabling
	 */
	@Override
	public void onDisable() {

	}

	/**
	 * Called when a player wants to open its enderchest by interacting with an enderchest block
	 * @param block The block clicked by the player
	 * @param player The player who interacts with the chest
	 * @return True if the block chest can be opened
	 */
	@Override
	public boolean onBlockChestOpened(Block block, Player player) {
		return this.factionHook.onBlockChestOpened(block, player);
	}

}

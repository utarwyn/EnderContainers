package fr.utarwyn.endercontainers.dependencies.faction;

import fr.utarwyn.endercontainers.dependencies.Dependency;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

/**
 * Dependency used to interact with the Factions plugin
 * @since 1.0.3
 * @author Utarwyn
 */
public class FactionsDependency extends Dependency {

	private FactionsHook factionHook;

	/**
	 * Called when the dependency is enabling
	 */
	@Override
	public void onEnable() {
		// Get Factions version
		String pluginVersion = this.getPluginVersion();

		if (pluginVersion != null) {
			String[] components = pluginVersion.split("\\.");
			String version = components.length < 2 ? "" : components[0] + "." + components[1];

			// Instanciate the correct hook in terms of the version of Factions
			if ("1.6".equals(version)) {
				// Old version of Factions - FactionsUUID - SavageFactions
				this.factionHook = new FactionsLegacyHook();
			} else {
				// New versions of Factions!
				this.factionHook = new FactionsV2Hook();
			}
		}
	}

	/**
	 * Called when the dependency is disabling
	 */
	@Override
	public void onDisable() {
		this.factionHook = null;
	}

	/**
	 * Called when a player wants to open its enderchest by interacting with an enderchest block
	 * @param block The block clicked by the player
	 * @param player The player who interacts with the chest
	 * @param sendMessage The plugin have to send a message to the player?
	 * @return True if the block chest can be opened
	 */
	@Override
	public boolean onBlockChestOpened(Block block, Player player, boolean sendMessage) {
		return this.factionHook == null || this.factionHook.onBlockChestOpened(block, player, sendMessage);
	}

}

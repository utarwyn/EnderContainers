package fr.utarwyn.endercontainers.dependency.faction;

import fr.utarwyn.endercontainers.dependency.Dependency;
import fr.utarwyn.endercontainers.dependency.DependencyListener;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

/**
 * Dependency used to interact with the Factions plugin
 *
 * @author Utarwyn
 * @since 1.0.3
 */
public class FactionsDependency extends Dependency {

    private DependencyListener factionHook;

    @Override
    public void onEnable() {
        String pluginVersion = this.getPluginVersion();

        if (pluginVersion != null) {
            String[] components = pluginVersion.split("\\.");
            String version = components.length < 2 ? "" : components[0] + "." + components[1];

            // Instanciate the correct hook in terms of the version of Factions
            if ("1.6".equals(version)) {
                // Old version of Factions - FactionsUUID - SavageFactions
                this.factionHook = new FactionsV1Hook();
            } else {
                this.factionHook = new FactionsV2Hook();
            }
        }
    }

    @Override
    public void onDisable() {
        this.factionHook = null;
    }

    /**
     * Called when a player wants to open its enderchest by interacting with an enderchest block
     *
     * @param block       The block clicked by the player
     * @param player      The player who interacts with the chest
     * @param sendMessage The plugin have to send a message to the player?
     * @return True if the block chest can be opened
     */
    @Override
    public boolean onBlockChestOpened(Block block, Player player, boolean sendMessage) {
        return this.factionHook == null || this.factionHook.onBlockChestOpened(block, player, sendMessage);
    }

}

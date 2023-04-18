package fr.utarwyn.endercontainers.enderchest.listener;

import fr.utarwyn.endercontainers.Managers;
import fr.utarwyn.endercontainers.configuration.Files;
import fr.utarwyn.endercontainers.dependency.DependenciesManager;
import fr.utarwyn.endercontainers.dependency.exceptions.BlockChestOpeningException;
import fr.utarwyn.endercontainers.enderchest.EnderChestManager;
import fr.utarwyn.endercontainers.enderchest.context.PlayerContext;
import fr.utarwyn.endercontainers.util.PluginMsg;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.world.WorldSaveEvent;

import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

/**
 * Class used to intercept events of player
 *
 * @author Utarwyn
 * @since 2.0.0
 */
public class EnderChestListener implements Listener {

    /**
     * The enderchest manager
     */
    private final EnderChestManager manager;

    /**
     * The dependencies manager
     */
    private final DependenciesManager dependenciesManager;

    /**
     * Construct the listener
     *
     * @param manager The chest manager associated with this listener
     */
    public EnderChestListener(EnderChestManager manager) {
        this.manager = manager;
        this.dependenciesManager = Managers.get(DependenciesManager.class);
    }

    /**
     * Method called when a player interacts with something in the world
     *
     * @param event The interact event
     */
    @EventHandler(ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!event.getAction().equals(Action.RIGHT_CLICK_BLOCK) || event.useInteractedBlock() == Event.Result.DENY) {
            return;
        }

        Player player = event.getPlayer();
        Block block = event.getClickedBlock();

        // Right click on an ender chest?
        if (block != null && block.getType().equals(Material.ENDER_CHEST)) {
            // Player is in sneaking mode with item in the hand?
            if (player.isSneaking() && !player.getInventory().getItemInHand().getType().equals(Material.AIR)) {
                return;
            }

            // Plugin not enabled or world disabled in config?
            if (Files.getConfiguration().getDisabledWorlds().contains(player.getWorld().getName())) {
                return;
            }

            event.setCancelled(true);

            // Check for dependencies if the player can interact with the block here
            try {
                this.dependenciesManager.validateBlockChestOpening(block, player);

                this.manager.loadPlayerContext(player.getUniqueId(),
                        context -> context.openListInventory(player, block));
            } catch (BlockChestOpeningException e) {
                if (e.getKey() != null) {
                    PluginMsg.errorMessage(player, e.getKey(), e.getParameters());
                }
            }
        }
    }

    /**
     * Method called when a world is saved
     *
     * @param event The save event
     */
    @EventHandler
    public void onWorldSave(WorldSaveEvent event) {
        World world = event.getWorld();

        // Iterate over all the player contexts to save them
        Map<UUID, PlayerContext> contextMap = this.manager.getContextMap();
        for (Iterator<Map.Entry<UUID, PlayerContext>> it = contextMap.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<UUID, PlayerContext> entry = it.next();

            // Only the player in the world of the event will be saved
            Player player = entry.getValue().getOwnerAsObject();
            if (player != null && world.equals(player.getWorld())) {
                UUID owner = entry.getKey();

                // Clear all the player data from memory
                boolean unused = this.manager.isContextUnused(owner);

                // Because iterator is used, we need to clear the player data manually with it.remove()
                this.manager.savePlayerContext(owner, false);
                if (unused) {
                    it.remove();
                }
            }
        }
    }

    /**
     * Method called when a player quits the server
     *
     * @param event The quit event
     */
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        UUID owner = event.getPlayer().getUniqueId();

        // Clear all the player data from memory
        boolean unused = this.manager.isContextUnused(owner);
        this.manager.savePlayerContext(owner, unused);
    }

}

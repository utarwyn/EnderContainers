package fr.utarwyn.endercontainers.enderchest;

import fr.utarwyn.endercontainers.Managers;
import fr.utarwyn.endercontainers.configuration.Files;
import fr.utarwyn.endercontainers.dependency.DependenciesManager;
import fr.utarwyn.endercontainers.util.MiscUtil;
import fr.utarwyn.endercontainers.util.Updater;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;

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
    private EnderChestManager manager;

    /**
     * The dependencies manager
     */
    private DependenciesManager dependenciesManager;

    /**
     * Construct the listener
     *
     * @param manager The chest manager associated with this listener
     */
    EnderChestListener(EnderChestManager manager) {
        this.manager = manager;
        this.dependenciesManager = Managers.get(DependenciesManager.class);
    }

    /**
     * Method called when a player interacts with something in the world
     *
     * @param event The interact event
     */
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) return;
        Player player = event.getPlayer();
        Block block = event.getClickedBlock();

        // Right click on an ender chest?
        if (block != null && block.getType().equals(Material.ENDER_CHEST)) {
            // Player is in sneaking mode with item in the hand?
            if (player.isSneaking() && !player.getInventory().getItemInMainHand().getType().equals(Material.AIR)) {
                return;
            }

            // Plugin not enabled or world disabled in config?
            if (Files.getConfiguration().getDisabledWorlds().contains(player.getWorld().getName())) {
                return;
            }

            event.setCancelled(true);

            // Check for dependencies if the player can interact with the block here
            if (this.dependenciesManager.onBlockChestOpened(block, player, true)) {
                this.manager.loadPlayerContext(player.getUniqueId(),
                        context -> context.openHubMenuFor(player, block));
            }
        }
    }

    /**
     * Method called when a player joins the server
     *
     * @param event The join event
     */
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // Send update message to the player is he has the permission.
        if (MiscUtil.playerHasPerm(player, "update") && Managers.get(Updater.class).notifyPlayer(player)) {
            MiscUtil.playSound(player, "NOTE_PLING", "BLOCK_NOTE_PLING");
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
        this.manager.saveAndDeletePlayerContext(owner);
    }

    /**
     * Method called when a player closes an inventory
     *
     * @param event The inventory close event
     */
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) return;

        Player player = (Player) event.getPlayer();
        Inventory inventory = event.getInventory();

        // Play the closing sound when we use the default enderchest!
        if (inventory.getType().equals(InventoryType.ENDER_CHEST) && Files.getConfiguration().isUseVanillaEnderchest()) {
            if (Files.getConfiguration().isGlobalSound()) {
                MiscUtil.playSound(player.getLocation(), "CHEST_CLOSE", "BLOCK_CHEST_CLOSE");
            } else {
                MiscUtil.playSound(player, "CHEST_CLOSE", "BLOCK_CHEST_CLOSE");
            }
        }
    }

}

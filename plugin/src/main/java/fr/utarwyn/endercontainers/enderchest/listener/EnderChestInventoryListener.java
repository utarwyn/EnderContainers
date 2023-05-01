package fr.utarwyn.endercontainers.enderchest.listener;

import fr.utarwyn.endercontainers.Managers;
import fr.utarwyn.endercontainers.compatibility.CompatibilityHelper;
import fr.utarwyn.endercontainers.configuration.Files;
import fr.utarwyn.endercontainers.enderchest.EnderChestManager;
import fr.utarwyn.endercontainers.inventory.EnderChestInventory;
import fr.utarwyn.endercontainers.inventory.InventoryManager;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

/**
 * Intercepts events about chest inventories.
 *
 * @author Utarwyn
 * @since 2.3.0
 */
public class EnderChestInventoryListener implements Listener {

    private final EnderChestManager manager;

    private final InventoryManager inventoryManager;

    public EnderChestInventoryListener(EnderChestManager manager) {
        this.manager = manager;
        this.inventoryManager = Managers.get(InventoryManager.class);
    }

    /**
     * Called when a player click in an inventory.
     * Used to detect an interaction with an enderchest inventory.
     *
     * @param event inventory click event
     */
    @EventHandler(priority = EventPriority.LOW)
    public void onInventoryClick(InventoryClickEvent event) {
        Inventory inventory = event.getView().getTopInventory();
        if (isEnderChestInventory(inventory) || inventory.getHolder() instanceof EnderChestInventory) {
            this.inventoryManager.cancelClickEventIfRestricted(event, this::checkIfMaterialIsRestricted);
        }
    }

    /**
     * Called when a player drag items in an enderchest inventory.
     * Used to detect and possibly cancel an interaction with an enderchest inventory.
     *
     * @param event inventory drag event
     */
    @EventHandler(priority = EventPriority.LOW)
    public void onInventoryDrag(InventoryDragEvent event) {
        Inventory inventory = event.getView().getTopInventory();
        if (isEnderChestInventory(inventory) || inventory.getHolder() instanceof EnderChestInventory) {
            this.inventoryManager.cancelDragEventIfRestricted(event, items -> items.length > 0 && this.checkIfMaterialIsRestricted(items[0]));
        }
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

        // Play the closing sound when we use the default enderchest!
        if (this.isEnderChestInventory(event.getInventory())) {
            // Play the closing sound
            Sound sound = CompatibilityHelper.searchSound("CHEST_CLOSE", "BLOCK_CHEST_CLOSE");
            if (Files.getConfiguration().isGlobalSound()) {
                player.getWorld().playSound(player.getLocation(), sound, 1f, 1f);
            } else {
                player.playSound(player.getLocation(), sound, 1f, 1f);
            }
        }
    }

    /**
     * Checks if an item material has been forbidden from enderchests.
     *
     * @param item item to check
     * @return true if material is forbidden, false otherwise
     */
    private boolean checkIfMaterialIsRestricted(ItemStack item) {
        return !Files.getConfiguration().getForbiddenMaterials().isEmpty() && item != null
                && Files.getConfiguration().getForbiddenMaterials().stream().anyMatch(material -> item.getType() == material);
    }

    /**
     * Checks if an inventory belongs to an enderchest managed by Bukkit
     * and should be handled in EnderContainers.
     *
     * @param inventory inventory to check
     * @return true if vanilla and should be handled, false otherwise
     */
    private boolean isEnderChestInventory(Inventory inventory) {
        return inventory.getType().equals(InventoryType.ENDER_CHEST) && Files.getConfiguration().isUseVanillaEnderchest();
    }

}

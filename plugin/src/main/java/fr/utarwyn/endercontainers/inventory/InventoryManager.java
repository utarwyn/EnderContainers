package fr.utarwyn.endercontainers.inventory;

import fr.utarwyn.endercontainers.AbstractManager;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;

import java.util.Optional;

/**
 * Manages all inventories of the plugin.
 * Listens for all events which may have an impact on plugin inventories.
 *
 * @author Utarwyn
 * @since 2.0.0
 */
public class InventoryManager extends AbstractManager {

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize() {
        this.registerListener(this);
    }

    /**
     * Called when a player click in an inventory.
     * Used to detect an interaction with one of registered inventories.
     *
     * @param event inventory click event
     */
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Inventory inventory = event.getView().getTopInventory();
        Optional<AbstractInventoryHolder> holder = this.getInventoryHolder(inventory);

        int slot = event.getRawSlot();
        boolean validSlot = slot >= 0 && slot < inventory.getSize();

        if (holder.isPresent()) {
            // A restricted move is when player clicks in the inventory or uses shift click
            boolean restrictedMove = validSlot || event.isShiftClick();
            event.setCancelled(this.isMoveItemRestricted(event.getWhoClicked(), holder.get()) && restrictedMove);

            // Perform the action only when player clicks on a valid slot of the inventory
            if (validSlot) {
                holder.get().onClick((Player) event.getWhoClicked(), slot);
            }
        }
    }

    /**
     * Called when a player drag items in an inventory.
     * Used to detect cancel an interaction with one of registered inventories.
     *
     * @param event inventory drag event
     */
    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        Inventory inventory = event.getView().getTopInventory();
        Optional<AbstractInventoryHolder> holder = this.getInventoryHolder(inventory);

        if (event.getWhoClicked() instanceof Player && holder.isPresent()
                && this.isMoveItemRestricted(event.getWhoClicked(), holder.get())) {
            boolean itemInInventory = event.getRawSlots().stream()
                    .anyMatch(slot -> slot < inventory.getSize());

            event.setCancelled(itemInInventory);
        }
    }

    /**
     * Called when a player close an inventory.
     * Dispatch the event to the internally managed inventory if found.
     *
     * @param event inventory close event
     */
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        Optional<AbstractInventoryHolder> holder = this.getInventoryHolder(event.getInventory());
        holder.ifPresent(h -> h.onClose((Player) event.getPlayer()));
    }

    /**
     * Close all managed inventories for everyone connected on the server.
     * MUST be called on the primary thread of the server.
     */
    public void closeAll() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            Inventory inventory = player.getOpenInventory().getTopInventory();
            Optional<AbstractInventoryHolder> holder = this.getInventoryHolder(inventory);

            if (holder.isPresent()) {
                player.closeInventory();
                holder.get().onClose(player);
            }

            if (InventoryType.ENDER_CHEST.equals(inventory.getType())) {
                player.closeInventory();
            }
        }
    }

    /**
     * Gets a plugin-specific holder object which manages an inventory.
     *
     * @param inventory inventory to check
     * @return inventory holder if found, otherwise null
     */
    private Optional<AbstractInventoryHolder> getInventoryHolder(Inventory inventory) {
        boolean isCustom = inventory.getHolder() instanceof AbstractInventoryHolder;
        return isCustom ? Optional.of((AbstractInventoryHolder) inventory.getHolder()) : Optional.empty();
    }

    /**
     * Checks if a player can move an item in a specific inventory or not.
     *
     * @param human  human entity instance
     * @param holder inventory holder where the item wants to be moved
     * @return false if the move is retricted for the player, false otherwise
     */
    private boolean isMoveItemRestricted(HumanEntity human, AbstractInventoryHolder holder) {
        return holder.isItemMovingRestricted() || GameMode.SPECTATOR == human.getGameMode();
    }

}

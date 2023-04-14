package fr.utarwyn.endercontainers.inventory;

import fr.utarwyn.endercontainers.AbstractManager;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;
import java.util.function.Predicate;

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
    @EventHandler(ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        Inventory inventory = event.getView().getTopInventory();
        this.getInventoryHolder(inventory).ifPresent(holder -> {
            this.cancelClickEventIfRestricted(event, item -> holder.isItemMovingRestricted());

            // Perform the action only when player clicks on a valid slot of the inventory
            int slot = event.getRawSlot();
            boolean validSlot = slot >= 0 && slot < inventory.getSize();
            if (validSlot) {
                holder.onClick((Player) event.getWhoClicked(), slot);
            }
        });
    }

    /**
     * Called when a player drag items in an inventory.
     * Used to detect cancel an interaction with one of registered inventories.
     *
     * @param event inventory drag event
     */
    @EventHandler(ignoreCancelled = true)
    public void onInventoryDrag(InventoryDragEvent event) {
        this.getInventoryHolder(event.getView().getTopInventory()).ifPresent(holder ->
                this.cancelDragEventIfRestricted(event, items -> holder.isItemMovingRestricted())
        );
    }

    /**
     * Called when a player close an inventory.
     * Dispatch the event to the internally managed inventory if found.
     *
     * @param event inventory close event
     */
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        this.getInventoryHolder(event.getInventory())
                .ifPresent(holder -> holder.onClose((Player) event.getPlayer()));
    }

    /**
     * Cancels an inventory click event based on its context.
     *
     * @param event         event to possibly cancel
     * @param itemPredicate predicate to test if used itemstack can trigger a cancellation
     */
    public void cancelClickEventIfRestricted(
            InventoryClickEvent event, Predicate<ItemStack> itemPredicate
    ) {
        Inventory topInventory = event.getView().getTopInventory();
        Inventory bottomInventory = event.getView().getBottomInventory();
        boolean validSlot = event.getRawSlot() >= 0 && event.getRawSlot() < topInventory.getSize();
        boolean isSpectator = GameMode.SPECTATOR == event.getWhoClicked().getGameMode();

        ItemStack item;
        if (event.isShiftClick()) {
            item = event.getCurrentItem();
        } else if (event.getAction() == InventoryAction.HOTBAR_SWAP) {
            item = bottomInventory.getItem(event.getHotbarButton());
        } else {
            item = event.getCursor();
        }

        event.setCancelled((validSlot || event.isShiftClick()) && (isSpectator || itemPredicate.test(item)));
    }

    /**
     * Cancels an inventory drag event based on its context.
     *
     * @param event          event to possibly cancel
     * @param itemsPredicate predicate to test if used itemstacks can trigger a cancellation
     */
    public void cancelDragEventIfRestricted(
            InventoryDragEvent event, Predicate<ItemStack[]> itemsPredicate
    ) {
        Inventory inventory = event.getView().getTopInventory();
        event.setCancelled(
                event.getRawSlots().stream().anyMatch(slot -> slot < inventory.getSize())
                        && (GameMode.SPECTATOR == event.getWhoClicked().getGameMode()
                        || itemsPredicate.test(event.getNewItems().values().toArray(new ItemStack[0])))
        );
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

}

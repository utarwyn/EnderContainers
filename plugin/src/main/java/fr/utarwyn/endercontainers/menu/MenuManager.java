package fr.utarwyn.endercontainers.menu;

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
 * Manages all menus of the plugin.
 * Used as a listener too, this class makes the creation
 * of menus a very simple action.
 *
 * @author Utarwyn
 * @since 2.0.0
 */
public class MenuManager extends AbstractManager {

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize() {
        this.registerListener(this);
    }

    /**
     * Called when a player click in an inventory.
     * Used to detect an interaction with one of registered menus.
     *
     * @param event inventory click event
     */
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Inventory inventory = event.getView().getTopInventory();
        Optional<AbstractMenu> menu = this.getMenuFromInventory(inventory);

        int slot = event.getRawSlot();
        boolean validSlot = slot >= 0 && slot < inventory.getSize();

        if (menu.isPresent()) {
            // A restricted move is when player clicks in the menu or uses shift click
            boolean restrictedMove = validSlot || event.isShiftClick();
            event.setCancelled(this.isMoveItemRestricted(event.getWhoClicked(), menu.get()) && restrictedMove);

            // Perform the action only when player clicks on a valid slot of the menu
            if (validSlot) {
                menu.get().onClick((Player) event.getWhoClicked(), slot);
            }
        }
    }

    /**
     * Called when a player drag items in an inventory.
     * Used to detect cancel an interaction with one of registered menus.
     *
     * @param event inventory drag event
     */
    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        Inventory inventory = event.getView().getTopInventory();
        Optional<AbstractMenu> menu = this.getMenuFromInventory(inventory);

        if (event.getWhoClicked() instanceof Player && menu.isPresent()
                && this.isMoveItemRestricted(event.getWhoClicked(), menu.get())) {
            boolean itemInMenu = event.getRawSlots().stream()
                    .anyMatch(slot -> slot < inventory.getSize());

            event.setCancelled(itemInMenu);
        }
    }

    /**
     * Called when a player close an inventory.
     * Used to detect a closure of one of registered menus.
     *
     * @param event The inventory close event
     */
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        Optional<AbstractMenu> menu = this.getMenuFromInventory(event.getInventory());
        menu.ifPresent(m -> m.onClose((Player) event.getPlayer()));
    }

    /**
     * Close all menus for everyone connected on the server.
     * MUST be called on the primary thread of the server.
     */
    public void closeAll() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            Inventory inventory = player.getOpenInventory().getTopInventory();
            Optional<AbstractMenu> menu = this.getMenuFromInventory(inventory);

            if (menu.isPresent()) {
                player.closeInventory();
                menu.get().onClose(player);
            }

            if (InventoryType.ENDER_CHEST.equals(inventory.getType())) {
                player.closeInventory();
            }
        }
    }

    /**
     * Get a menu whiches manage an inventory.
     *
     * @param inventory Inventory to check
     * @return Menu if found, otherwise null
     */
    private Optional<AbstractMenu> getMenuFromInventory(Inventory inventory) {
        boolean isCustom = inventory.getHolder() instanceof AbstractMenu;
        return isCustom ? Optional.of((AbstractMenu) inventory.getHolder()) : Optional.empty();
    }

    /**
     * Checks if a player can move an item in a specific menu or not.
     *
     * @param human human entity instance
     * @param menu  menu where the item wants to be moved
     * @return false if the move is retricted for the player, false otherwise
     */
    private boolean isMoveItemRestricted(HumanEntity human, AbstractMenu menu) {
        return menu.isItemMovingRestricted() || GameMode.SPECTATOR == human.getGameMode();
    }

}

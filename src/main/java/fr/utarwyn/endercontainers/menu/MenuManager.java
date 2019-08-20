package fr.utarwyn.endercontainers.menu;

import fr.utarwyn.endercontainers.AbstractManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

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
     * @param event The inventory click event
     */
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Inventory inventory = event.getView().getTopInventory();

        if (event.getRawSlot() < inventory.getSize()) {
            AbstractMenu menu = this.getMenuFromInventory(inventory);
            Player player = (Player) event.getWhoClicked();
            ItemStack item = event.getCurrentItem();

            if (menu != null && item != null && !Material.AIR.equals(item.getType())) {
                event.setCancelled(menu.onClick(player, event.getSlot()));
            }
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
        AbstractMenu menu = this.getMenuFromInventory(event.getInventory());

        if (menu != null) {
            menu.onClose((Player) event.getPlayer());
        }
    }

    /**
     * Close all menus for everyone connected on the server.
     */
    public void closeAll() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            Inventory inventory = player.getOpenInventory().getTopInventory();
            AbstractMenu menu = this.getMenuFromInventory(inventory);

            if (menu != null) {
                player.closeInventory();
                menu.onClose(player);
            }

            if (InventoryType.ENDER_CHEST.equals(inventory.getType()) || menu != null) {
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
    private AbstractMenu getMenuFromInventory(Inventory inventory) {
        return inventory.getHolder() instanceof AbstractMenu ? (AbstractMenu) inventory.getHolder() : null;
    }

}

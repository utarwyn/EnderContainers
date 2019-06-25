package fr.utarwyn.endercontainers.menu;

import fr.utarwyn.endercontainers.EnderContainers;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages all menus of the plugin.
 * Used as a listener too, this class makes the creation
 * of menus a very simple action.
 *
 * @author Utarwyn
 * @since 2.0.0
 */
public class Menus implements Listener {

    /**
     * Collection of all menus
     */
    private static Set<AbstractMenu> menuSet;

    /**
     * No constructor, its an utility class
     */
    private Menus() {
    }

    /**
     * Register an AbstractMenu
     *
     * @param menu Menu to register
     */
    static void registerMenu(AbstractMenu menu) {
        if (menuSet == null) {
            menuSet = ConcurrentHashMap.newKeySet();
            Bukkit.getPluginManager().registerEvents(new Menus(), EnderContainers.getInstance());
        }

        menuSet.add(menu);
    }

    /**
     * Unregister an AbstractMenu to liberate memory
     *
     * @param menu Menu to unregister
     */
    static void unregisterMenu(AbstractMenu menu) {
        if (menuSet != null) {
            menuSet.remove(menu);
        }
    }

    /**
     * Close all registered menus for everyone connected on the server
     */
    public static void closeAll() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            Inventory openInv = player.getOpenInventory().getTopInventory();
            if (InventoryType.ENDER_CHEST.equals(openInv.getType())) {
                player.closeInventory();
                continue;
            }

            if (menuSet != null) {
                for (AbstractMenu menu : menuSet) {
                    if (openInv.getHolder() == menu) {
                        menu.updateItems();
                        menu.onClose(player);

                        player.closeInventory();
                        break;
                    }
                }
            }
        }
    }

    /**
     * Called when a player click in an inventory.
     * Used to detect an interaction with one of registered menus.
     *
     * @param event The inventory click event
     */
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        // Only detect clicks inside the top inventory of the view!
        Inventory inventory = event.getView().getTopInventory();

        if (event.getRawSlot() < inventory.getSize()) {
            AbstractMenu menu = this.getMenuFromInventory(inventory);
            Player player = (Player) event.getWhoClicked();

            if (menu == null || event.getSlot() < 0)
                return;
            if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR)
                return;

            event.setCancelled(menu.onClick(player, event.getSlot()));
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
        Player player = (Player) event.getPlayer();
        if (menu == null) return;

        menu.updateItems();
        menu.onClose(player);
    }

    /**
     * Gets a registered menu linked to a given inventory
     *
     * @param inventory Inventory to check
     * @return Menu if found, otherwise null
     */
    private AbstractMenu getMenuFromInventory(Inventory inventory) {
        for (AbstractMenu menu : menuSet)
            if (menu == inventory.getHolder())
                return menu;

        return null;
    }

}

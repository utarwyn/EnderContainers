package fr.utarwyn.endercontainers.menu;

import fr.utarwyn.endercontainers.configuration.Files;
import fr.utarwyn.endercontainers.enderchest.EnderChest;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A derived class of the Enderchest menu to manage
 * the saving of vanilla enderchest for offline players.
 * Because we need to save manually the data on the disk
 * to save correctly the enderchest.
 *
 * @author Utarwyn
 * @see fr.utarwyn.endercontainers.enderchest.EnderChestListener#onInventoryClose(InventoryCloseEvent)
 * @since 2.0.0
 */
public class OfflineEnderChestMenu extends EnderChestMenu {

    /**
     * Map which stores the opened menus to perform
     * because its the only way to know which menu is opened
     * by online players because we cant add a custom holder
     * for a Bukkit enderchest inventory.
     *
     * @see fr.utarwyn.endercontainers.enderchest.EnderChestListener#onInventoryClose(InventoryCloseEvent)
     */
    private static ConcurrentHashMap<UUID, EnderChest> openedMenus;

    static {
        openedMenus = new ConcurrentHashMap<>();
    }

    /**
     * Constructs the offline chest menu
     *
     * @param enderChest Enderchest to link with this menu
     */
    public OfflineEnderChestMenu(EnderChest enderChest) {
        super(enderChest);
    }

    /**
     * Returns which enderchest is opened by a specific player
     * and remove it from the cache to avoid memory leaks.
     *
     * @param player Player who closes the inventory
     * @return Enderchest opened by the given player
     */
    public static EnderChest getOpenedChestFor(Player player) {
        return openedMenus.remove(player.getUniqueId());
    }

    @Override
    public void open(Player player) {
        if (this.enderChest.getNum() == 0 && Files.getConfiguration().isUseVanillaEnderchest()) {
            openedMenus.put(player.getUniqueId(), this.enderChest);
        }

        super.open(player);
    }

}

package fr.utarwyn.endercontainers.menu;

import fr.utarwyn.endercontainers.EnderContainers;
import fr.utarwyn.endercontainers.configuration.Files;
import fr.utarwyn.endercontainers.enderchest.EnderChest;
import fr.utarwyn.endercontainers.util.EUtil;
import fr.utarwyn.endercontainers.util.uuid.UUIDFetcher;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Represents an enderchest with all contents.
 *
 * @author Utarwyn
 * @since 2.0.0
 */
public class EnderChestMenu extends AbstractMenu {

    /**
     * Enderchest who generated this menu
     */
    EnderChest enderChest;

    /**
     * Construct a menu which will contains contents of the enderchest
     *
     * @param enderChest The enderchest
     */
    public EnderChestMenu(EnderChest enderChest) {
        super(
                enderChest.getRows(),
                Files.getLocale().getMenuChestTitle()
                        .replace("%player%", Objects.requireNonNull(UUIDFetcher.getName(enderChest.getOwner())))
                        .replace("%num%", String.valueOf(enderChest.getNum() + 1))
        );

        this.enderChest = enderChest;
    }

    /**
     * Get all contents of the menu
     *
     * @return A map with all contents
     */
    public ConcurrentHashMap<Integer, ItemStack> getContents() {
        return this.items;
    }

    @Override
    public void prepare() {
        // No preparation needed for this menu
    }

    @Override
    public int getFilledSlotsNb() {
        // Use the default enderchest to calculate the filled slots.
        if (this.enderChest.getNum() == 0 && Files.getConfiguration().isUseVanillaEnderchest()) {
            Inventory chest = EUtil.getVanillaEnderchestOf(this.enderChest.getOwner());
            assert chest != null;

            return EUtil.getInventorySize(chest);
        }

        return super.getFilledSlotsNb();
    }

    @Override
    public boolean onClick(Player player, int slot) {
        return false;
    }

    @Override
    public void onClose(Player player) {
        if (EnderContainers.getInstance().isEnabled()) {
            Bukkit.getScheduler().runTaskAsynchronously(EnderContainers.getInstance(), this.enderChest::save);

            if (Files.getConfiguration().isGlobalSound()) {
                EUtil.playSound(player.getLocation(), "CHEST_CLOSE", "BLOCK_CHEST_CLOSE");
            } else {
                EUtil.playSound(player, "CHEST_CLOSE", "BLOCK_CHEST_CLOSE");
            }
        } else {
            // Do the save synchronously because the plugin is disabling...
            this.enderChest.save();
        }
    }

    @Override
    public void open(Player player) {
        if (this.enderChest.getNum() == 0 && Files.getConfiguration().isUseVanillaEnderchest()) {
            Inventory chest = EUtil.getVanillaEnderchestOf(this.enderChest.getOwner());
            assert chest != null;

            player.openInventory(chest);
        } else {
            super.open(player);
        }
    }

}

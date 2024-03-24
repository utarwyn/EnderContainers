package fr.utarwyn.endercontainers.inventory;

import com.google.common.base.Preconditions;
import fr.utarwyn.endercontainers.Managers;
import fr.utarwyn.endercontainers.compatibility.CompatibilityHelper;
import fr.utarwyn.endercontainers.configuration.Files;
import fr.utarwyn.endercontainers.configuration.LocaleKey;
import fr.utarwyn.endercontainers.configuration.enderchests.SaveMode;
import fr.utarwyn.endercontainers.enderchest.EnderChest;
import fr.utarwyn.endercontainers.enderchest.EnderChestManager;
import fr.utarwyn.endercontainers.util.uuid.UUIDFetcher;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Objects;
import java.util.concurrent.ConcurrentMap;

/**
 * Represents a custom enderchest with all contents.
 *
 * @author Utarwyn
 * @since 2.0.0
 */
public class EnderChestInventory extends AbstractInventoryHolder {

    /**
     * Enderchest who generated this inventory
     */
    private final EnderChest chest;

    /**
     * Internal map to cache all contents of the chest (even those not displayed in the container).
     */
    private ConcurrentMap<Integer, ItemStack> contents;

    /**
     * Constructs an inventory which contains contents of an enderchest.
     *
     * @param chest The enderchest
     */
    public EnderChestInventory(EnderChest chest) {
        this.chest = chest;
        this.itemMovingRestricted = false;

        this.reloadInventory();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void prepare() {
        this.contents = this.chest.getContents();

        // Add all items in the container (only those which can be displayed)
        int size = this.chest.getMaxSize();
        this.contents.forEach((index, item) -> {
            if (index < size) {
                this.inventory.setItem(index, item);
            }
        });
    }

    /**
     * Retrieve all contents of the chest.
     *
     * @return map with all items (even those which are out of bounds)
     */
    public ConcurrentMap<Integer, ItemStack> getContents() {
        return this.contents;
    }

    /**
     * Updates the whole content of this chest, based on its container.
     * Check first for all items in the container, but take also those which are in cache (not displayed).
     */
    public void updateContentsFromContainer() {
        Preconditions.checkNotNull(this.inventory, "container seems to be null");
        Preconditions.checkNotNull(this.contents, "internal contents map seems to be null");

        ItemStack[] containerContents = this.inventory.getContents();

        // Replace cache contents with container contents if filled
        for (int i = 0; i < containerContents.length; i++) {
            if (containerContents[i] != null) {
                this.contents.put(i, containerContents[i]);
            } else {
                this.contents.remove(i);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected int getRows() {
        return this.chest.getRows();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getTitle() {
        String num = String.valueOf(this.chest.getNum() + 1);
        String playername = Objects.requireNonNull(UUIDFetcher.getName(this.chest.getOwner()));

        return Files.getLocale().getMessage(LocaleKey.MENU_CHEST_TITLE)
                .replace("%player%", playername)
                .replace("%num%", num);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onClose(Player player) {
        // Save and delete the player context if the owner of the chest is offline
        Player owner = Bukkit.getPlayer(this.chest.getOwner());
        boolean offlineOwner = owner == null || !owner.isOnline();

        // Save chest inventory if owner is offline or forced by the configuration
        if (offlineOwner || Files.getConfiguration().getSaveMode() == SaveMode.ON_CLOSE) {
            EnderChestManager enderChestManager = Managers.get(EnderChestManager.class);
            enderChestManager.savePlayerContext(this.chest.getOwner());
            if (offlineOwner) {
                enderChestManager.deletePlayerContextIfUnused(this.chest.getOwner());
            }
        }

        // Play the closing sound
        Sound sound = CompatibilityHelper.searchSound("CHEST_CLOSE", "BLOCK_CHEST_CLOSE");
        if (Files.getConfiguration().isGlobalSound()) {
            player.getWorld().playSound(player.getLocation(), sound, 1f, 1f);
        } else {
            player.playSound(player.getLocation(), sound, 1f, 1f);
        }
    }

}

package fr.utarwyn.endercontainers.menu.enderchest;

import fr.utarwyn.endercontainers.Managers;
import fr.utarwyn.endercontainers.configuration.Files;
import fr.utarwyn.endercontainers.configuration.LocaleKey;
import fr.utarwyn.endercontainers.enderchest.EnderChest;
import fr.utarwyn.endercontainers.enderchest.EnderChestManager;
import fr.utarwyn.endercontainers.menu.AbstractMenu;
import fr.utarwyn.endercontainers.util.MiscUtil;
import fr.utarwyn.endercontainers.util.uuid.UUIDFetcher;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Objects;

/**
 * Represents a custom enderchest with all contents.
 *
 * @author Utarwyn
 * @since 2.0.0
 */
public class EnderChestMenu extends AbstractMenu {

    /**
     * Enderchest who generated this menu
     */
    private EnderChest chest;

    /**
     * Construct a menu whiches contain contents of an enderchest.
     *
     * @param chest The enderchest
     */
    public EnderChestMenu(EnderChest chest) {
        this.chest = chest;
        this.itemMovingRestricted = false;

        this.reloadInventory();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void prepare() {
        int size = this.chest.getMaxSize();
        this.chest.getContents().forEach((index, item) -> {
            if (index < size) {
                this.inventory.setItem(index, item);
            }
        });
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
        if (owner == null || !owner.isOnline()) {
            Managers.get(EnderChestManager.class).savePlayerContext(this.chest.getOwner(), true);
        }

        // Play the closing sound
        if (Files.getConfiguration().isGlobalSound()) {
            MiscUtil.playSound(player.getLocation(), "CHEST_CLOSE", "BLOCK_CHEST_CLOSE");
        } else {
            MiscUtil.playSound(player, "CHEST_CLOSE", "BLOCK_CHEST_CLOSE");
        }
    }

}

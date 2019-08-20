package fr.utarwyn.endercontainers.menu.enderchest;

import fr.utarwyn.endercontainers.EnderContainers;
import fr.utarwyn.endercontainers.configuration.Files;
import fr.utarwyn.endercontainers.enderchest.EnderChest;
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
    EnderChest chest;

    /**
     * Construct a menu whiches contain contents of an enderchest.
     *
     * @param chest The enderchest
     */
    public EnderChestMenu(EnderChest chest) {
        this.chest = chest;
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

        return Files.getLocale().getMenuChestTitle()
                .replace("%player%", playername)
                .replace("%num%", num);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onClick(Player player, int slot) {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onClose(Player player) {
        if (EnderContainers.getInstance().isEnabled()) {
            Bukkit.getScheduler().runTaskAsynchronously(
                    EnderContainers.getInstance(), () -> this.chest.save(this.getMapContents())
            );

            if (Files.getConfiguration().isGlobalSound()) {
                MiscUtil.playSound(player.getLocation(), "CHEST_CLOSE", "BLOCK_CHEST_CLOSE");
            } else {
                MiscUtil.playSound(player, "CHEST_CLOSE", "BLOCK_CHEST_CLOSE");
            }
        } else {
            // Do the save synchronously because the plugin is disabling...
            this.chest.save(this.getMapContents());
        }
    }

}

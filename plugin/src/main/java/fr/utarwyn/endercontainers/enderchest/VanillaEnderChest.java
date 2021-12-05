package fr.utarwyn.endercontainers.enderchest;

import fr.utarwyn.endercontainers.EnderContainers;
import fr.utarwyn.endercontainers.compatibility.nms.NMSPlayerUtil;
import fr.utarwyn.endercontainers.enderchest.context.PlayerContext;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.Arrays;
import java.util.Objects;
import java.util.logging.Level;

/**
 * Represents the Bukkit enderchest of a player.
 *
 * @author Utarwyn
 * @since 2.2.0
 */
public class VanillaEnderChest extends EnderChest {

    /**
     * Owner of the enderchest.
     * We need a Player instance to get the enderchest.
     */
    private Player owner;

    /**
     * Construct a new vanilla enderchest.
     *
     * @param context context of the player's chest
     */
    public VanillaEnderChest(PlayerContext context) {
        super(context, 0);
        this.owner = context.getOwnerAsObject();
    }

    /**
     * Get the owner of the enderchest as a Player object.
     *
     * @return player object
     */
    public Player getOwnerAsPlayer() {
        return this.owner;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isContainerUsed() {
        return this.owner != null && !this.owner.getEnderChest().getViewers().isEmpty();
    }

    /**
     * Check if a specific is using this inventory.
     *
     * @param player player to check
     * @return true if the player is using this container
     */
    public boolean isUsedBy(Player player) {
        return this.owner != null && this.owner.getEnderChest().getViewers().contains(player);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getSize() {
        if (this.owner != null) {
            Inventory inventory = this.owner.getEnderChest();
            return (int) Arrays.stream(inventory.getContents()).filter(Objects::nonNull).count();
        } else {
            return 0;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void openContainerFor(Player player) {
        if (this.owner != null) {
            player.openInventory(this.owner.getEnderChest());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateRowCount() {
        this.rows = 3;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateContainer() {
        // No custom container attached
    }

    /**
     * Retrieves the offline player profile from server data.
     * MUST be called in a synchronous way.
     */
    public void loadOfflinePlayer() {
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(context.getOwner());
        try {
            this.owner = NMSPlayerUtil.get().loadPlayer(offlinePlayer);
        } catch (ReflectiveOperationException e) {
            EnderContainers.getInstance().getLogger().log(Level.SEVERE, String.format(
                    "Cannot get the profile of player %s", context.getOwner()
            ), e);
        }
    }

}

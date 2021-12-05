package fr.utarwyn.endercontainers.enderchest;

import fr.utarwyn.endercontainers.configuration.Files;
import fr.utarwyn.endercontainers.enderchest.context.PlayerContext;
import fr.utarwyn.endercontainers.inventory.EnderChestInventory;
import fr.utarwyn.endercontainers.util.MiscUtil;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;
import java.util.concurrent.ConcurrentMap;

/**
 * Represents a custom enderchest of a player.
 *
 * @author Utarwyn
 * @since 2.0.0
 */
public class EnderChest {

    /**
     * The number of the enderchest.
     */
    private final int num;
    /**
     * Player context for which the enderchest has been loaded.
     */
    protected PlayerContext context;

    /**
     * Amount of rows of the enderchest.
     */
    protected int rows;
    /**
     * Inventory which contains contents of this enderchest.
     */
    EnderChestInventory container;

    /**
     * Construct a new enderchest.
     *
     * @param context context of the player's chest
     * @param num     number of the enderchest
     */
    public EnderChest(PlayerContext context, int num) {
        this.context = context;
        this.num = num;
        this.updateRowCount();
        this.updateContainer();
    }

    /**
     * Returns the number of the chest
     *
     * @return Number of the enderchest
     */
    public int getNum() {
        return this.num;
    }

    /**
     * Returns the owner of the chest.
     *
     * @return UUID of the chest's owner
     */
    public UUID getOwner() {
        return this.context.getOwner();
    }

    /**
     * Returns the size of the chest
     *
     * @return The size of the chest (Number of filled slots)
     */
    public int getSize() {
        return this.container.getFilledSlotsNb();
    }

    /**
     * Returns the number of rows
     *
     * @return Number of rows
     */
    public int getRows() {
        return this.rows;
    }

    /**
     * Returns the number of slots in the chest
     *
     * @return Number of slots in the chest.
     */
    public int getMaxSize() {
        return this.rows * 9;
    }

    /**
     * Returns the fill percentage of the inventory
     *
     * @return The fill percentage
     */
    public double getFillPercentage() {
        return (double) this.getSize() / (this.rows * 9);
    }

    /**
     * Returns contents of this enderchest.
     *
     * @return content of the enderchest
     */
    public ConcurrentMap<Integer, ItemStack> getContents() {
        if (this.container != null && this.container.isInitialized()) {
            return this.container.getContents();
        } else {
            return this.context.getData().getEnderchestContents(this);
        }
    }

    /**
     * Check the accessibility of the chest
     *
     * @return True if the chest is accessible
     */
    public boolean isAccessible() {
        Player player = this.context.getOwnerAsObject();

        // For the first chest or if the player is offline, viewer has the access.
        if (!this.isDefault() && player != null) {
            return MiscUtil.playerHasPerm(player, "open." + this.getNum());
        } else {
            return true;
        }
    }

    /**
     * Check if the container is used by someone on the server.
     *
     * @return true if at least one player is using it
     */
    public boolean isContainerUsed() {
        return this.container.isUsed();
    }

    /**
     * Returns true if the chest was enabled by default
     *
     * @return True if the chest is unlocked for everyone
     */
    public boolean isDefault() {
        return this.num < Files.getConfiguration().getDefaultEnderchests();
    }

    /**
     * Check if the container is empty.
     *
     * @return True if the container is empty
     */
    public boolean isEmpty() {
        return this.getSize() == 0;
    }

    /**
     * Check if the container is full.
     *
     * @return True if the container is full
     */
    public boolean isFull() {
        return this.getSize() == this.getMaxSize();
    }

    /**
     * Open the chest container for a specific player.
     *
     * @param player The player who wants to open the container
     */
    public void openContainerFor(Player player) {
        // update chest metadata before opening
        this.updateRowCount();
        this.updateContainer();

        this.container.open(player);
    }

    /**
     * Retrieves the amount of rows accessible by a player.
     * Computes player permissions if connected, or the cache otherwise.
     */
    public void updateRowCount() {
        int count = 3;
        Player player = this.context.getOwnerAsObject();

        // player not connected, retrieve the amount from the cache
        if (player == null) {
            this.rows = this.context.getData().getEnderchestRows(this);
            return;
        }

        // check player permissions to compute the row count for this slot
        for (int perm = 6; perm > 0; perm--) {
            if ((MiscUtil.playerHasPerm(player, "slot" + this.num + ".row" + perm)) ||
                    (MiscUtil.playerHasPerm(player, "slots.row" + perm))) {
                count = perm;
                break;
            }
        }

        this.rows = count;
    }

    /**
     * Updates or creates custom container of the chest.
     */
    public void updateContainer() {
        if (this.container != null) {
            // if chest row count has changed, also reload the container
            boolean needReload = this.container.getInventory().getSize() != getMaxSize();

            if (needReload) {
                this.container.close();
            }

            // at least we would like to update container contents
            this.container.updateContentsFromContainer();

            if (needReload) {
                this.container.reloadInventory();
            }
        } else {
            this.container = new EnderChestInventory(this);
        }
    }

}

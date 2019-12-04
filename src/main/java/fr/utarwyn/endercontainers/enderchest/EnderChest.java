package fr.utarwyn.endercontainers.enderchest;

import fr.utarwyn.endercontainers.configuration.Files;
import fr.utarwyn.endercontainers.menu.enderchest.EnderChestMenu;
import fr.utarwyn.endercontainers.storage.StorageWrapper;
import fr.utarwyn.endercontainers.storage.player.PlayerData;
import fr.utarwyn.endercontainers.util.MiscUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentMap;

/**
 * Class used to create a custom enderchest
 *
 * @author Utarwyn
 * @since 2.0.0
 */
public class EnderChest {

    /**
     * The number of the enderchest
     */
    private int num;

    /**
     * Owner of the enderchest
     */
    private UUID owner;

    /**
     * Menu whiches can contain contents of this enderchest
     */
    private EnderChestMenu container;

    /**
     * The number of rows of the chest's menu
     */
    private int rows;

    /**
     * Allows to create a new enderchest
     *
     * @param owner The owner's UUID of the chest
     * @param num   The number of the enderchest
     */
    public EnderChest(UUID owner, int num) {
        this.owner = owner;
        this.num = num;
        this.rows = this.calculateRowCount();
        this.container = new EnderChestMenu(this);
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
        return this.owner;
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
        return (double) this.container.getFilledSlotsNb() / (this.rows * 9);
    }

    /**
     * Returns contents of this enderchest.
     *
     * @return content of the enderchest
     */
    public ConcurrentMap<Integer, ItemStack> getContents() {
        return this.getOwnerData().getEnderchestContents(this);
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
     * Allow to know if the chest is managed by the server.
     *
     * @return true if the chest is a vanilla one
     */
    public boolean isVanilla() {
        return this.num == 0 && Files.getConfiguration().isUseVanillaEnderchest();
    }

    /**
     * Returns the accessibility of the chest
     *
     * @return True if the chest is accessible
     */
    public boolean isAccessible() {
        Player player = this.getOwnerPlayer();

        // For the first chest or if the player is offline, viewer has the access.
        if (!this.isDefault() && player != null) {
            return MiscUtil.playerHasPerm(player, "open." + this.getNum());
        } else {
            return true;
        }
    }

    /**
     * Returns true if the container is full
     *
     * @return True if the container is full
     */
    public boolean isFull() {
        return this.getSize() == this.getMaxSize();
    }

    /**
     * Returns true if the container is empty
     *
     * @return True if the container is empty
     */
    public boolean isEmpty() {
        return this.getSize() == 0;
    }

    /**
     * Open the chest container for a specific player
     *
     * @param player The player who wants to open the container
     */
    public void openContainerFor(Player player) {
        if (this.isVanilla()) {
            Player ownerObj = this.getOwnerPlayer();
            // Owner must be online
            if (ownerObj != null && ownerObj.isOnline()) {
                player.openInventory(ownerObj.getEnderChest());
            }
            // TODO Support opening chests of offline players
        } else {
            this.container.open(player);
        }
    }

    /**
     * Save the enderchest. This method has to be called asynchronously if possible!
     */
    public void save(ConcurrentMap<Integer, ItemStack> contents) {
        this.getOwnerData().saveEnderchest(this, contents);
    }

    /**
     * Get the number of rows accessible if the player
     * is connected on the server. This number is generated in terms
     * of permissions of the player.
     * (That's why it have to be connected)
     *
     * @return The number of rows generated or null if the player is not connected
     */
    private int calculateRowCount() {
        int row = 3;

        // Use the vanilla chest!
        if (this.isVanilla()) return row;

        Player player = this.getOwnerPlayer();

        // No player connected for this chest, use the cache instead.
        if (player == null) {
            return this.getOwnerData().getEnderchestRows(this);
        }

        for (int iRow = 6; iRow > 0; iRow--) {
            if (MiscUtil.playerHasPerm(player, "slot" + this.num + ".row" + iRow)
                    || MiscUtil.playerHasPerm(player, "slots.row" + iRow)) {
                row = iRow;
                break;
            }
        }

        return row;
    }

    /**
     * Get the owner as a Player object
     *
     * @return The Player object of the owner if he is connected, null otherwise
     */
    private Player getOwnerPlayer() {
        return Bukkit.getPlayer(this.owner);
    }

    /**
     * Get data of the owner of the chest.
     *
     * @return data of the chest's owner
     */
    private PlayerData getOwnerData() {
        return Objects.requireNonNull(StorageWrapper.get(PlayerData.class, this.owner));
    }

}

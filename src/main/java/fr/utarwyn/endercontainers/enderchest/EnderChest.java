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
     * True if the chest is accessible
     */
    private boolean accessible;

    /**
     * Allows to create a new enderchest
     *
     * @param owner The owner's UUID of the chest
     * @param num   The number of the enderchest
     */
    EnderChest(UUID owner, int num) {
        this.owner = owner;
        this.num = num;

        this.load();
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
     * Returns the accessibility of the chest
     *
     * @return True if the chest is accessible
     */
    public boolean isAccessible() {
        return this.accessible;
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
     * Returns true if the chest was not used
     * (not used mean that the owner is offline and the container has no viewer
     * so it have to be cleared from the memory)
     *
     * @return True if the chest is unused
     * @see EnderChestPurgeTask Task which uses this method to clear unused data.
     */
    boolean isUnused() {
        return this.getOwnerPlayer() == null && this.container.getInventory().getViewers().isEmpty();
    }

    /**
     * Open the chest container for a specific player
     *
     * @param player The player who wants to open the container
     */
    public void openContainerFor(Player player) {
        if (this.num == 0 && Files.getConfiguration().isUseVanillaEnderchest()) {
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
     * Reload all metas of the chest
     * (that means the number of rows and the accessibility of the chest)
     */
    public void reloadMeta() {
        // Detection of number of rows ...
        Integer rowsNb = this.generateRowsNb();
        if (rowsNb == null) {
            rowsNb = this.getOwnerData().getEnderchestRows(this);
        }

        this.rows = rowsNb;

        // Load the accessibility of the enderchest ...
        Boolean accessibility = this.genereateAccessibility();

        /*
         * If the accessibility cannot be resolved, it means that the player is offline.
         * So, it means that the enderchest will be opened by an administrator,
         * and in this case, the chest have to be accessible.
         */
        this.accessible = accessibility != null ? accessibility : true;
    }

    /**
     * Load the chest at the creation of it
     */
    private void load() {
        // Reload metas of the chest!
        this.reloadMeta();

        // Load the container for an online/offline player.
        this.container = new EnderChestMenu(this);
    }

    /**
     * Get the number of rows accessible if the player
     * is connected on the server. This number is generated in terms
     * of permissions of the player.
     * (That's why it have to be connected)
     *
     * @return The number of rows generated or null if the player is not connected
     */
    private Integer generateRowsNb() {
        Player player = this.getOwnerPlayer();

        // No player connected for this chest, use the cache instead.
        if (player == null) return null;
        // Use the vanilla chest!
        if (this.num == 0 && Files.getConfiguration().isUseVanillaEnderchest()) return 3;

        for (int row = 6; row > 0; row--)
            if (MiscUtil.playerHasPerm(player, "slot" + this.num + ".row" + row) || MiscUtil.playerHasPerm(player, "slots.row" + row))
                return row;

        return 3;
    }

    /**
     * Get the accessibility of the chest if the player
     * is connected on the server. This value is generated in terms
     * of permissions of the player.
     * (That's why it have to be connected)
     *
     * @return The accessibility generated or null if the player is not connected
     */
    private Boolean genereateAccessibility() {
        Player player = this.getOwnerPlayer();

        if (this.isDefault()) return true;
        if (player == null) return null;

        return MiscUtil.playerHasPerm(player, "open." + this.getNum());
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

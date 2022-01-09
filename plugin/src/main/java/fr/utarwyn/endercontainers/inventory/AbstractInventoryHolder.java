package fr.utarwyn.endercontainers.inventory;

import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

/**
 * Manages an inventory created by the plugin.
 * Adds some features to {@link org.bukkit.inventory.InventoryHolder} class.
 *
 * @author Utarwyn
 * @since 2.0.0
 */
public abstract class AbstractInventoryHolder implements InventoryHolder {

    /**
     * The generated inventory
     */
    protected Inventory inventory;

    /**
     * True if the inventory has been initialized, false otherwise
     */
    private boolean initialized;

    /**
     * Returns the number of filled slots in the container.
     *
     * @return Number of fileld slots
     */
    public int getFilledSlotsNb() {
        return (int) Arrays.stream(this.inventory.getContents()).filter(Objects::nonNull).count();
    }

    /**
     * Returns inventory managed by this holder.
     *
     * @return inventory managed by this holder
     */
    @Override
    public Inventory getInventory() {
        return this.inventory;
    }

    /**
     * Get the loading state of the inventory.
     *
     * @return true if the inventory has been initialized
     */
    public boolean isInitialized() {
        return this.initialized;
    }

    /**
     * Check if somebody is viewing the inventory.
     *
     * @return true if there is at least one player viewing the inventory
     */
    public boolean isUsed() {
        return this.inventory != null && !this.inventory.getViewers().isEmpty();
    }

    /**
     * Called when a player click on an item in the inventory
     *
     * @param player The player who interacts with the inventory
     * @param slot   The slot where the player has clicked
     */
    public void onClick(Player player, int slot) {
        // Not implemented
    }

    /**
     * Check if a specific item can be moved inside the inventory.
     *
     * @param itemStack item that wants to be moved
     * @return true if the item can be moved, false otherwise
     */
    public boolean canMoveItemInside(ItemStack itemStack) {
        return false;
    }

    /**
     * Open the container to a specific player.
     *
     * @param player Player that will receive the container
     */
    public void open(Player player) {
        player.openInventory(this.inventory);
    }

    /**
     * Closes the inventory for all viewers.
     */
    public void close() {
        if (this.inventory != null) {
            new ArrayList<>(this.inventory.getViewers()).forEach(HumanEntity::closeInventory);
        }
    }

    /**
     * Recreates the inventory with existing contents.
     */
    public void reloadInventory() {
        String title = this.getTitle();
        int size = this.getRows() * 9;
        ItemStack[] itemStacks = new ItemStack[size];

        if (title.length() > 32) {
            title = title.substring(0, 32);
        }

        if (this.inventory != null) {
            ItemStack[] oldContents = this.inventory.getContents();
            itemStacks = Arrays.copyOfRange(oldContents, 0, Math.min(oldContents.length, size));
        }

        this.inventory = Bukkit.createInventory(this, size, title);
        this.inventory.setContents(itemStacks);

        this.prepare();

        this.initialized = true;
    }

    /**
     * Called when a player closes the inventory
     *
     * @param player player who closes the inventory
     */
    public abstract void onClose(Player player);

    /**
     * Return the number of rows needed for this container.
     *
     * @return the number of rows of this inventory
     */
    protected abstract int getRows();

    /**
     * Prepare this inventory by adding needed items directly inside the current inventory.
     */
    protected abstract void prepare();

    /**
     * Return the title displayed at the top of this container.
     *
     * @return the displayed title
     */
    protected abstract String getTitle();

}

package fr.utarwyn.endercontainers.storage.player;

import fr.utarwyn.endercontainers.enderchest.EnderChest;
import fr.utarwyn.endercontainers.storage.StorageWrapper;
import org.bukkit.inventory.ItemStack;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Logger;

/**
 * Storage wrapper to manage data of a specific player.
 *
 * @author Utarwyn
 * @since 2.0.0
 */
public abstract class PlayerData extends StorageWrapper {

    /**
     * UUID of the player linked to this wrapper
     */
    protected UUID uuid;

    /**
     * Construct a new storage wrapper for a player (even offline)
     *
     * @param logger plugin logger
     * @param uuid   The uuid of the player to manage its data
     */
    public PlayerData(Logger logger, UUID uuid) {
        super(logger);
        this.uuid = uuid;
    }

    /**
     * Save a full player context in the storage.
     *
     * @param chests chests to save
     */
    public void saveContext(Set<EnderChest> chests) {
        chests.forEach(this::saveEnderchest);
        this.save();
    }

    /**
     * Returns contents of a saved enderchest
     *
     * @param enderChest Get this enderchest's contents
     * @return The map filled with the contents of the chest
     */
    public abstract ConcurrentMap<Integer, ItemStack> getEnderchestContents(EnderChest enderChest);

    /**
     * Returns the number of rows saved for an enderchest
     *
     * @param chest Get this enderchest's number of rows
     * @return Number of rows stored for the enderchest
     */
    public abstract int getEnderchestRows(EnderChest chest);

    /**
     * Save all data of an enderchest.
     *
     * @param chest enderchest to save
     */
    protected abstract void saveEnderchest(EnderChest chest);

}

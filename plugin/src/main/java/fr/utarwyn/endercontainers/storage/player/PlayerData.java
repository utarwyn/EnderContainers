package fr.utarwyn.endercontainers.storage.player;

import fr.utarwyn.endercontainers.EnderContainers;
import fr.utarwyn.endercontainers.enderchest.EnderChest;
import fr.utarwyn.endercontainers.enderchest.VanillaEnderChest;
import fr.utarwyn.endercontainers.storage.StorageWrapper;
import fr.utarwyn.endercontainers.storage.serialization.ItemSerializer;
import org.bukkit.inventory.ItemStack;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Level;

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
    protected final UUID uuid;

    /**
     * Serializer to use when storing itemstacks
     */
    private final ItemSerializer itemSerializer;

    /**
     * Construct a new storage wrapper for a player (even offline)
     *
     * @param uuid           The uuid of the player to manage its data
     * @param plugin         plugin instance object
     * @param itemSerializer serializer object to use with this storage object
     */
    protected PlayerData(UUID uuid, EnderContainers plugin, ItemSerializer itemSerializer) {
        super(plugin);
        this.uuid = uuid;
        this.itemSerializer = itemSerializer;
    }

    /**
     * Save a full player context in the storage.
     *
     * @param chests chests to save
     */
    public void saveContext(Set<EnderChest> chests) {
        chests.stream().filter(chest -> !(chest instanceof VanillaEnderChest))
                .forEach(this::saveEnderchest);

        this.save();
    }

    protected String serializeChestContents(EnderChest chest) {
        try {
            return this.itemSerializer.serialize(chest.getContents());
        } catch (IOException e) {
            this.plugin.getLogger().log(Level.WARNING, String.format(
                    "cannot serialize items of the chest #%d of %s",
                    chest.getNum(), chest.getOwner()
            ), e);
            return null;
        }
    }

    protected ConcurrentMap<Integer, ItemStack> deserializeItems(EnderChest chest, String data) {
        try {
            return this.itemSerializer.deserialize(data);
        } catch (IOException e) {
            this.plugin.getLogger().log(Level.WARNING, String.format(
                    "cannot deserialize items of the chest #%d of %s",
                    chest.getNum(), chest.getOwner()
            ), e);
            return new ConcurrentHashMap<>();
        }
    }

    /**
     * Returns contents of a saved enderchest
     *
     * @param chest Get this enderchest's contents
     * @return The map filled with the contents of the chest
     */
    public abstract ConcurrentMap<Integer, ItemStack> getEnderchestContents(EnderChest chest);

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

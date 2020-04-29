package fr.utarwyn.endercontainers.storage.player;

import fr.utarwyn.endercontainers.enderchest.EnderChest;
import fr.utarwyn.endercontainers.storage.FlatFile;
import fr.utarwyn.endercontainers.util.ItemSerializer;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Storage wrapper for player data (flatfile)
 *
 * @author Utarwyn
 * @since 2.0.0
 */
public class PlayerFlatData extends PlayerData {

    /**
     * Prefix used in the config file to store player data
     */
    private static final String PREFIX = "enderchests";

    /**
     * Object which manages interaction with a flat file
     */
    private FlatFile flatFile;

    /**
     * Construct a new player storage wrapper with a flat file.
     *
     * @param logger plugin logger
     * @param uuid   player's uuid
     */
    public PlayerFlatData(Logger logger, UUID uuid) {
        super(logger, uuid);
        this.load();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void load() {
        try {
            String minimalUuid = this.uuid.toString().replace("-", "");
            this.flatFile = new FlatFile("data" + File.separator + minimalUuid + ".yml");
        } catch (IOException e) {
            this.logger.log(Level.SEVERE, String.format(
                    "Cannot load the data file of the user %s", this.uuid
            ), e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void save() {
        try {
            this.flatFile.save();
        } catch (IOException e) {
            this.logger.log(Level.SEVERE, String.format(
                    "Cannot save the data file of the user %s", this.uuid
            ), e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ConcurrentMap<Integer, ItemStack> getEnderchestContents(EnderChest enderChest) {
        String path = PREFIX + "." + enderChest.getNum() + ".contents";

        if (!this.flatFile.getConfiguration().contains(path)) {
            return new ConcurrentHashMap<>();
        }

        return ItemSerializer.deserialize(this.flatFile.getConfiguration().getString(path));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getEnderchestRows(EnderChest chest) {
        String path = PREFIX + "." + chest.getNum() + ".rows";
        return this.flatFile.getConfiguration().contains(path) ?
                this.flatFile.getConfiguration().getInt(path) : 3;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveEnderchest(EnderChest chest) {
        String path = PREFIX + "." + chest.getNum();
        String contents = !chest.getContents().isEmpty() ?
                ItemSerializer.serialize(chest.getContents()) : null;

        this.flatFile.getConfiguration().set(path + ".rows", chest.getRows());
        this.flatFile.getConfiguration().set(path + ".position", chest.getNum());
        this.flatFile.getConfiguration().set(path + ".contents", contents);
    }

}

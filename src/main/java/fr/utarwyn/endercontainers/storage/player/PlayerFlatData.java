package fr.utarwyn.endercontainers.storage.player;

import fr.utarwyn.endercontainers.enderchest.EnderChest;
import fr.utarwyn.endercontainers.storage.FlatFile;
import fr.utarwyn.endercontainers.util.ItemSerializer;
import org.bukkit.inventory.ItemStack;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Level;

/**
 * Storage wrapper for player data (flatfile)
 *
 * @author Utarwyn
 * @since 2.0.0
 */
public class PlayerFlatData extends PlayerData {

    /**
     * Prefix used in the config file to store player data.
     */
    private static final String PREFIX = "enderchests";

    private FlatFile flatFile;

    PlayerFlatData(UUID uuid) {
        super(uuid);
    }

    @Override
    protected void load() {
        try {
            this.flatFile = new FlatFile("data/" + this.getMinimalUUID() + ".yml");
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Cannot load the data file of the user" + this.getUUID(), e);
        }
    }

    @Override
    protected void save() {
        try {
            this.flatFile.save();
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Cannot save the data file of the user " + this.getUUID(), e);
        }
    }

    @Override
    public ConcurrentMap<Integer, ItemStack> getEnderchestContents(EnderChest enderChest) {
        String path = PREFIX + "." + enderChest.getNum() + ".contents";

        if (!this.flatFile.getConfiguration().contains(path)) {
            return new ConcurrentHashMap<>();
        }

        return ItemSerializer.deserialize(this.flatFile.getConfiguration().getString(path));
    }

    @Override
    public int getEnderchestRows(EnderChest enderChest) {
        String path = PREFIX + "." + enderChest.getNum() + ".rows";
        return this.flatFile.getConfiguration().contains(path) ? this.flatFile.getConfiguration().getInt(path) : 3;
    }

    @Override
    public void saveEnderchest(EnderChest chest, ConcurrentMap<Integer, ItemStack> contents) {
        String path = PREFIX + "." + chest.getNum() + ".";

        this.flatFile.getConfiguration().set(path + "rows", chest.getRows());
        this.flatFile.getConfiguration().set(path + "position", chest.getNum());
        this.flatFile.getConfiguration().set(path + "contents", ItemSerializer.serialize(contents));
        this.flatFile.getConfiguration().set(path + "lastlocking", System.currentTimeMillis() / 1000); // UNIX format

        this.save();
    }

}

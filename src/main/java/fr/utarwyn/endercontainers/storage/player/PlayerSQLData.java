package fr.utarwyn.endercontainers.storage.player;

import fr.utarwyn.endercontainers.database.DatabaseSet;
import fr.utarwyn.endercontainers.enderchest.EnderChest;
import fr.utarwyn.endercontainers.util.ItemSerializer;
import org.bukkit.inventory.ItemStack;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Level;

/**
 * Storage wrapper for player data (MySQL)
 *
 * @author Utarwyn
 * @since 2.0.0
 */
public class PlayerSQLData extends PlayerData {

    private List<DatabaseSet> enderchestsDataset;

    PlayerSQLData(UUID uuid) {
        super(uuid);
    }

    @Override
    protected void load() {
        this.enderchestsDataset = getDatabaseManager().getEnderchestsOf(this.uuid);
    }

    @Override
    protected void save() {
        // There is no file to save when using SQL
    }

    @Override
    public ConcurrentMap<Integer, ItemStack> getEnderchestContents(EnderChest enderChest) {
        for (DatabaseSet chestSet : this.enderchestsDataset)
            if (chestSet.getInteger("num") == enderChest.getNum())
                return ItemSerializer.deserialize(chestSet.getString("contents"));

        return new ConcurrentHashMap<>();
    }

    @Override
    public int getEnderchestRows(EnderChest enderChest) {
        for (DatabaseSet chestSet : this.enderchestsDataset)
            if (chestSet.getInteger("num") == enderChest.getNum())
                return chestSet.getInteger("rows");

        return 3;
    }

    @Override
    protected void saveEnderchest(EnderChest enderChest) {
        boolean insert = true;

        for (DatabaseSet set : this.enderchestsDataset)
            if (set.getInteger("num") == enderChest.getNum() && set.getString("owner").equals(enderChest.getOwner().toString())) {
                insert = false;
                break;
            }

        String contents = ItemSerializer.serialize(enderChest.getContents());

        try {
            getDatabaseManager().saveEnderchest(insert, enderChest.getOwner(),
                    enderChest.getNum(), enderChest.getRows(), contents);
        } catch (SQLException e) {
            logger.log(Level.SEVERE,
                    "Cannot save the enderchest for user " + enderChest.getOwner() + " in the database", e);
            return;
        }

        // If this is a new enderchest, we need to store it in memory.
        if (insert) {
            DatabaseSet set = new DatabaseSet();
            set.setObject("num", enderChest.getNum());
            set.setObject("owner", enderChest.getOwner().toString());
            set.setObject("contents", contents);
            set.setObject("rows", enderChest.getRows());

            this.enderchestsDataset.add(set);
        }
    }

}


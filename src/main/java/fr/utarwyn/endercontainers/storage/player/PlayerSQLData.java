package fr.utarwyn.endercontainers.storage.player;

import fr.utarwyn.endercontainers.Managers;
import fr.utarwyn.endercontainers.database.DatabaseManager;
import fr.utarwyn.endercontainers.database.DatabaseSet;
import fr.utarwyn.endercontainers.enderchest.EnderChest;
import fr.utarwyn.endercontainers.util.ItemSerializer;
import org.bukkit.inventory.ItemStack;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Storage wrapper for player data (MySQL)
 *
 * @author Utarwyn
 * @since 2.0.0
 */
public class PlayerSQLData extends PlayerData {

    /**
     * The database manager
     */
    private DatabaseManager databaseManager;

    /**
     * List of all enderchest datasets retreived from the database.
     */
    private List<DatabaseSet> enderchestSets;

    /**
     * Construct a new player storage wrapper with a SQL database.
     *
     * @param logger plugin logger
     * @param uuid   player's uuid
     */
    public PlayerSQLData(Logger logger, UUID uuid) {
        super(logger, uuid);
        this.databaseManager = Managers.get(DatabaseManager.class);
        this.load();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void load() {
        try {
            this.enderchestSets = this.databaseManager.getEnderchestsOf(this.uuid);
        } catch (SQLException e) {
            this.logger.log(Level.SEVERE, "Cannot retrieve enderchests of user " + this.uuid + " from the database", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void save() {
        // There is no file to save when using SQL
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ConcurrentMap<Integer, ItemStack> getEnderchestContents(EnderChest enderChest) {
        for (DatabaseSet chestSet : this.enderchestSets) {
            if (chestSet.getInteger("num") == enderChest.getNum()) {
                return ItemSerializer.deserialize(chestSet.getString("contents"));
            }
        }

        return new ConcurrentHashMap<>();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getEnderchestRows(EnderChest chest) {
        Optional<Integer> rows = this.enderchestSets.stream()
                .filter(set -> set.getInteger("num") == chest.getNum())
                .map(set -> set.getInteger("rows"))
                .findFirst();

        return rows.orElse(3);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveEnderchest(EnderChest enderChest) {
        boolean insert = this.enderchestSets.stream()
                .noneMatch(set -> set.getInteger("num") == enderChest.getNum()
                        && set.getString("owner").equals(enderChest.getOwner().toString()));

        String contents = ItemSerializer.serialize(enderChest.getContents());

        try {
            this.databaseManager.saveEnderchest(insert,
                    enderChest.getOwner(), enderChest.getNum(),
                    enderChest.getRows(), contents
            );
        } catch (SQLException e) {
            this.logger.log(Level.SEVERE, "Cannot save the enderchest for user " +
                    enderChest.getOwner() + " in the database", e);
            return;
        }

        // If this is a new enderchest, we need to store it in memory.
        if (insert) {
            DatabaseSet set = new DatabaseSet();
            set.setObject("num", enderChest.getNum());
            set.setObject("owner", enderChest.getOwner().toString());
            set.setObject("contents", contents);
            set.setObject("rows", enderChest.getRows());

            this.enderchestSets.add(set);
        }
    }

}


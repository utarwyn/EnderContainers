package fr.utarwyn.endercontainers.storage.player;

import fr.utarwyn.endercontainers.EnderContainers;
import fr.utarwyn.endercontainers.Managers;
import fr.utarwyn.endercontainers.database.DatabaseManager;
import fr.utarwyn.endercontainers.database.DatabaseSet;
import fr.utarwyn.endercontainers.enderchest.EnderChest;
import fr.utarwyn.endercontainers.storage.serialization.ItemSerializer;
import org.bukkit.inventory.ItemStack;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
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

    /**
     * The database manager
     */
    private final DatabaseManager databaseManager;

    /**
     * List of all enderchest datasets retreived from the database.
     */
    private List<DatabaseSet> databaseSets;

    /**
     * Construct a new player storage wrapper with a SQL database.
     *
     * @param uuid           player's uuid
     * @param plugin         plugin instance object
     * @param itemSerializer object to encode/decode itemstacks
     */
    public PlayerSQLData(UUID uuid, EnderContainers plugin, ItemSerializer itemSerializer) {
        super(uuid, plugin, itemSerializer);
        this.databaseManager = Managers.get(DatabaseManager.class);
        this.load();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void load() {
        try {
            this.databaseSets = this.databaseManager.getEnderchestsOf(this.uuid);
        } catch (SQLException e) {
            this.plugin.getLogger().log(Level.SEVERE, String.format(
                    "Cannot retrieve enderchests of user %s from the database", this.uuid
            ), e);
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
    public ConcurrentMap<Integer, ItemStack> getEnderchestContents(EnderChest chest) {
        for (DatabaseSet set : this.databaseSets) {
            if (set.getInteger("num") == chest.getNum()) {
                String contents = set.getString("contents");

                if (contents != null) {
                    return this.deserializeItems(chest, contents);
                } else {
                    break;
                }
            }
        }

        return new ConcurrentHashMap<>();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getEnderchestRows(EnderChest chest) {
        Optional<Integer> rows = this.databaseSets.stream()
                .filter(set -> set.getInteger("num") == chest.getNum())
                .map(set -> set.getInteger("rows"))
                .findFirst();

        return rows.orElse(3);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveEnderchest(EnderChest chest) {
        boolean insert = this.databaseSets.stream()
                .noneMatch(set -> set.getInteger("num") == chest.getNum()
                        && set.getString("owner").equals(chest.getOwner().toString()));

        String contents = !chest.getContents().isEmpty() ? this.serializeChestContents(chest) : null;

        try {
            this.databaseManager.saveEnderchest(
                    insert, chest.getOwner(),
                    chest.getNum(), chest.getRows(), contents
            );
        } catch (SQLException e) {
            this.plugin.getLogger().log(Level.SEVERE, String.format(
                    "Cannot save enderchest %d of user %s in the database",
                    chest.getNum(), chest.getOwner()
            ), e);
            return;
        }

        // If this is a new enderchest, we need to store it in memory.
        if (insert) {
            DatabaseSet set = new DatabaseSet();
            set.setObject("num", chest.getNum());
            set.setObject("owner", chest.getOwner().toString());
            set.setObject("contents", contents);
            set.setObject("rows", chest.getRows());

            this.databaseSets.add(set);
        }
    }

}


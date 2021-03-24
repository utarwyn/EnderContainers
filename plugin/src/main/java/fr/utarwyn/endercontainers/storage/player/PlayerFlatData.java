package fr.utarwyn.endercontainers.storage.player;

import fr.utarwyn.endercontainers.EnderContainers;
import fr.utarwyn.endercontainers.enderchest.EnderChest;
import fr.utarwyn.endercontainers.storage.serialization.ItemSerializer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Level;

/**
 * Manages player data thanks to a configuration file.
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
     * Storage file native object
     */
    private final File file;

    /**
     * Configuration file object of the player data
     */
    FileConfiguration configuration;

    /**
     * Construct a new player storage wrapper with a flat file.
     *
     * @param uuid           player's uuid
     * @param plugin         plugin instance object
     * @param itemSerializer object to encode/decode itemstacks
     */
    public PlayerFlatData(UUID uuid, EnderContainers plugin, ItemSerializer itemSerializer) {
        super(uuid, plugin, itemSerializer);

        String minimalUuid = this.uuid.toString().replace("-", "");
        this.file = new File(this.plugin.getDataFolder(), "data" + File.separator + minimalUuid + ".yml");

        this.load();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void load() {
        this.configuration = YamlConfiguration.loadConfiguration(this.file);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void save() {
        try {
            this.configuration.save(this.file);
        } catch (IOException | NullPointerException e) {
            this.plugin.getLogger().log(Level.SEVERE, String.format(
                    "Cannot save player data to %s", this.file.getPath()
            ), e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ConcurrentMap<Integer, ItemStack> getEnderchestContents(EnderChest chest) {
        String path = PREFIX + "." + chest.getNum() + ".contents";

        if (this.configuration.contains(path)) {
            return this.deserializeItems(chest, this.configuration.getString(path));
        }

        return new ConcurrentHashMap<>();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getEnderchestRows(EnderChest chest) {
        String path = PREFIX + "." + chest.getNum() + ".rows";
        return this.configuration.contains(path) ? this.configuration.getInt(path) : 3;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveEnderchest(EnderChest chest) {
        String path = PREFIX + "." + chest.getNum();
        String contents = !chest.getContents().isEmpty() ?
                this.serializeChestContents(chest) : null;

        this.configuration.set(path + ".rows", chest.getRows());
        this.configuration.set(path + ".position", chest.getNum());
        this.configuration.set(path + ".contents", contents);
    }

}

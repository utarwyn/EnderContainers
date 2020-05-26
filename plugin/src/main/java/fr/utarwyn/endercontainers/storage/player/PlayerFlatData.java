package fr.utarwyn.endercontainers.storage.player;

import fr.utarwyn.endercontainers.EnderContainers;
import fr.utarwyn.endercontainers.configuration.wrapper.YamlFileLoadException;
import fr.utarwyn.endercontainers.configuration.wrapper.YamlFileWrapper;
import fr.utarwyn.endercontainers.enderchest.EnderChest;
import fr.utarwyn.endercontainers.storage.serialization.ItemSerializer;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Level;

/**
 * Storage wrapper to manage player data through a Yaml file.
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
     * Object which manages configuration and data of the player
     */
    private final YamlFileWrapper configuration;

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
        this.configuration = new YamlFileWrapper(new File(
                this.plugin.getDataFolder(), "data" + File.separator + minimalUuid + ".yml"
        ));

        this.load();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void load() {
        try {
            this.configuration.load();
        } catch (YamlFileLoadException e) {
            this.plugin.getLogger().log(Level.SEVERE, String.format(
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
            this.configuration.save();
        } catch (IOException e) {
            this.plugin.getLogger().log(Level.SEVERE, String.format(
                    "Cannot save the data file of the user %s", this.uuid
            ), e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ConcurrentMap<Integer, ItemStack> getEnderchestContents(EnderChest chest) {
        String path = PREFIX + "." + chest.getNum() + ".contents";

        if (this.configuration.get().contains(path)) {
            return this.deserializeItems(chest, this.configuration.get().getString(path));
        }

        return new ConcurrentHashMap<>();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getEnderchestRows(EnderChest chest) {
        String path = PREFIX + "." + chest.getNum() + ".rows";
        return this.configuration.get().contains(path) ?
                this.configuration.get().getInt(path) : 3;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveEnderchest(EnderChest chest) {
        String path = PREFIX + "." + chest.getNum();
        String contents = !chest.getContents().isEmpty() ?
                this.serializeChestContents(chest) : null;

        this.configuration.get().set(path + ".rows", chest.getRows());
        this.configuration.get().set(path + ".position", chest.getNum());
        this.configuration.get().set(path + ".contents", contents);
    }

}

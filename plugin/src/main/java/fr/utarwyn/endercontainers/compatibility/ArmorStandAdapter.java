package fr.utarwyn.endercontainers.compatibility;

import fr.utarwyn.endercontainers.compatibility.bukkit.BukkitArmorStandAdapter;
import fr.utarwyn.endercontainers.compatibility.nms.NMSArmorStandAdapter;
import fr.utarwyn.endercontainers.hologram.HologramException;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

/**
 * Adapter interface for managing armor stands as holographic displays.
 * This interface provides methods to spawn and destroy armor stands for specific players,
 * typically used for version-specific implementations.
 *
 * @author Utarwyn
 * @since 2.3.0
 */
public interface ArmorStandAdapter {

    /**
     * Spawns an armor stand with text at a specific location for a player.
     *
     * @param plugin   the plugin instance
     * @param observer the player who will see the armor stand
     * @param location the location where to spawn the armor stand
     * @param text     the text to display above the armor stand
     * @return the entity ID of the spawned armor stand
     */
    int spawnArmorStandFor(Plugin plugin, Player observer, Location location, String text) throws HologramException;

    /**
     * Destroys an armor stand for a specific player.
     *
     * @param observer the player who can see the armor stand
     * @param entityId the entity ID of the armor stand to destroy
     */
    void destroyArmorStandFor(Player observer, int entityId) throws HologramException;

    /**
     * Creates a new armor stand adapter instance based on the server version.
     *
     * @return a new armor stand adapter instance
     */
    static ArmorStandAdapter create() {
        return ServerVersion.isNewerThan(ServerVersion.V1_18)
                ? new BukkitArmorStandAdapter()
                : new NMSArmorStandAdapter();
    }

}

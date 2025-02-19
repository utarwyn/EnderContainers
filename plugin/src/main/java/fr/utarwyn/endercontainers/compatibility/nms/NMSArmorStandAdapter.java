package fr.utarwyn.endercontainers.compatibility.nms;

import fr.utarwyn.endercontainers.compatibility.ArmorStandAdapter;
import fr.utarwyn.endercontainers.hologram.HologramException;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

/**
 * Adapter for managing armor stands as holographic displays.
 * This adapter provides methods to spawn and destroy armor stands for specific players.
 * This adapter is used for NMS implementations prior MC 1.19.
 *
 * @author Utarwyn
 * @since 2.3.0
 */
public class NMSArmorStandAdapter implements ArmorStandAdapter {

    @Override
    public int spawnArmorStandFor(Plugin plugin, Player observer, Location location, String text) throws HologramException {
        try {
            return NMSHologramUtil.get().spawnHologram(location, text, observer);
        } catch (ReflectiveOperationException cause) {
            throw new HologramException("cannot spawn hologram entity", cause);
        }
    }

    @Override
    public void destroyArmorStandFor(Player observer, int entityId) throws HologramException {
        if (!observer.isOnline()) {
            return;
        }
        try {
            NMSHologramUtil.get().destroyEntity(entityId, observer);
        } catch (ReflectiveOperationException cause) {
            throw new HologramException("cannot destroy hologram entity", cause);
        }
    }

}

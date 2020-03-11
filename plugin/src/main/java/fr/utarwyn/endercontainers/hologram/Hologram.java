package fr.utarwyn.endercontainers.hologram;

import fr.utarwyn.endercontainers.EnderContainers;
import fr.utarwyn.endercontainers.compatibility.nms.NMSHologramUtil;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.logging.Level;

/**
 * This class is used to display a text above an enderchest block
 * if the option blockNametag was set to true.
 * This class uses packets and supports versions from 1.8 to 1.15.
 *
 * @author Utarwyn
 * @since 2.0.0
 */
class Hologram {

    /**
     * Static field used to calculate the offset between each line
     * in an hologram.
     */
    static final double LINE_HEIGHT = 0.23D;

    /**
     * The title of the hologram (its content)
     */
    private String title;

    /**
     * The location where the hologram have to spawn
     */
    private Location location;

    /**
     * The player who has to receive the hologram
     */
    private Player observer;

    /**
     * Identifier of the spawned entity for the observer
     */
    private int entityId;

    /**
     * Construct an hologram and spawn it directly
     *
     * @param observer The observer who has to receive the hologram
     * @param title    The title/content of the hologram
     * @param location The location of the hologram
     */
    Hologram(Player observer, String title, Location location) {
        this.observer = observer;
        this.title = title;
        this.location = location;
        this.entityId = -1;

        this.spawn();
    }

    /**
     * Know if the player linked to the hologram is online
     *
     * @return True if the player is online
     */
    boolean isPlayerOnline() {
        return this.observer != null && this.observer.isOnline();
    }

    /**
     * Destroy the hologram for the observer with the stored entity id.
     */
    void destroy() {
        if (this.entityId > -1) {
            try {
                NMSHologramUtil.destroyEntity(this.entityId, this.observer);
            } catch (ReflectiveOperationException e) {
                EnderContainers.getInstance().getLogger().log(Level.SEVERE, "Cannot destroy the hologram", e);
            }
        }
    }

    /**
     * Spawn the hologram
     */
    private void spawn() {
        try {
            this.entityId = NMSHologramUtil.spawnHologram(this.location, this.title, this.observer);
        } catch (ReflectiveOperationException e) {
            EnderContainers.getInstance().getLogger().log(Level.SEVERE, "Cannot spawn the hologram", e);
        }
    }

}

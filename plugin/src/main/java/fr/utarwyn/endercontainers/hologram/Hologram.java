package fr.utarwyn.endercontainers.hologram;

import fr.utarwyn.endercontainers.compatibility.nms.NMSHologramUtil;
import org.bukkit.Location;
import org.bukkit.entity.Player;

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
    private final String title;

    /**
     * The location where the hologram have to spawn
     */
    private final Location location;

    /**
     * The player who has to receive the hologram
     */
    private final Player observer;

    /**
     * Identifier of the spawned entity for the observer
     */
    private Integer entityId;

    /**
     * Construct an hologram and spawn it directly
     *
     * @param observer The observer who has to receive the hologram
     * @param title    The title/content of the hologram
     * @param location The location of the hologram
     * @throws HologramException thrown if cannot spawn the hologram
     */
    Hologram(Player observer, String title, Location location) throws HologramException {
        this.observer = observer;
        this.title = title;
        this.location = location;

        this.spawn();
    }

    /**
     * Know if the player linked to the hologram is online
     *
     * @return True if the player is online
     */
    boolean isObserverOnline() {
        return this.observer != null && this.observer.isOnline();
    }

    /**
     * Destroy the hologram for the observer with the stored entity id.
     *
     * @throws HologramException thrown if cannot destroy the hologram
     */
    void destroy() throws HologramException {
        if (this.entityId != null && this.entityId >= 0) {
            try {
                NMSHologramUtil.get().destroyEntity(this.entityId, this.observer);
            } catch (ReflectiveOperationException cause) {
                throw new HologramException("cannot destroy hologram entity", cause);
            }
        }
    }

    /**
     * Spawn the hologram.
     *
     * @throws HologramException thrown if cannot spawn the hologram
     */
    private void spawn() throws HologramException {
        try {
            this.entityId = NMSHologramUtil.get().spawnHologram(this.location, this.title, this.observer);
        } catch (ReflectiveOperationException cause) {
            throw new HologramException("cannot spawn hologram entity", cause);
        }
    }

}

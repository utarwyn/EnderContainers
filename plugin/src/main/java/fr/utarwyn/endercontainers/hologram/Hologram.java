package fr.utarwyn.endercontainers.hologram;

import org.bukkit.entity.Player;

/**
 * This class is used to display a text above an enderchest block
 * if the option blockNametag was set to true.
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
     * The player who has to receive the hologram
     */
    private final Player observer;

    /**
     * Identifier of the spawned entity for the observer
     */
    private final Integer entityId;

    /**
     * Construct an hologram and spawn it directly
     *
     * @param observer The observer who has to receive the hologram
     * @param entityId The entity id of the hologram
     */
    Hologram(Player observer, int entityId) {
        this.observer = observer;
        this.entityId = entityId;
    }

    public Player getObserver() {
        return observer;
    }

    public Integer getEntityId() {
        return entityId;
    }

    /**
     * Know if the player linked to the hologram is online
     *
     * @return True if the player is online
     */
    boolean isObserverOnline() {
        return this.observer != null && this.observer.isOnline();
    }

}

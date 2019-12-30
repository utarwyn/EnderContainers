package fr.utarwyn.endercontainers.hologram;

import fr.utarwyn.endercontainers.EnderContainers;
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
    private static final double ABS = 0.23D;

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
    private Player player;

    /**
     * Identifier of the spawned entity for the observer
     */
    private int entityId;

    /**
     * Construct an hologram and spawn it directly
     *
     * @param player   The player who has to receive the hologram
     * @param title    The title/content of the hologram
     * @param location The location of the hologram
     */
    Hologram(Player player, String title, Location location) {
        this.player = player;
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
        return this.player != null && this.player.isOnline();
    }

    /**
     * Destroy the hologram for the observer with the stored entity id.
     */
    void destroy() {
        if (this.entityId > -1) {
            try {
                NMSHologramUtil.destroyEntity(this.entityId, this.player);
            } catch (ReflectiveOperationException e) {
                EnderContainers.getInstance().getLogger().log(Level.SEVERE, "Cannot destroy the hologram", e);
            }
        }
    }

    /**
     * Spawn the hologram
     */
    private void spawn() {
        Location displayLoc = this.location.clone().add(.5, ABS - 1.25D, .5);

        try {
            this.entityId = NMSHologramUtil.spawnHologram(this.player, displayLoc, this.title);
        } catch (ReflectiveOperationException e) {
            EnderContainers.getInstance().getLogger().log(Level.SEVERE, "Cannot spawn the hologram", e);
        }
    }

}

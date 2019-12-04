package fr.utarwyn.endercontainers.enderchest.context;

import fr.utarwyn.endercontainers.configuration.Files;
import fr.utarwyn.endercontainers.enderchest.EnderChest;
import fr.utarwyn.endercontainers.menu.enderchest.EnderChestHubMenu;
import fr.utarwyn.endercontainers.util.MiscUtil;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * A context in which all enderchests of a player are loaded.
 *
 * @author Utarwyn
 * @since 2.3.0
 */
public class PlayerContext {

    /**
     * List of all chests loaded in the context
     */
    private List<EnderChest> chests;

    /**
     * Owner of this memory context
     */
    private UUID owner;

    /**
     * Construct a new player context.
     *
     * @param owner  owner of the context
     * @param chests chests loaded in the context
     */
    PlayerContext(UUID owner, List<EnderChest> chests) {
        this.owner = owner;
        this.chests = chests;
    }

    /**
     * Get the owner of the context.
     *
     * @return owner uuid
     */
    public UUID getOwner() {
        return this.owner;
    }

    /**
     * Searching in the context for a chest which has a specific number.
     *
     * @param num The number of the chest
     * @return chest found
     */
    public Optional<EnderChest> getChest(int num) {
        return this.chests.stream().filter(ch -> ch.getNum() == num).findFirst();
    }

    /**
     * Permits to open the Hub menu with the list of enderchests
     * of a specific player to another human.
     *
     * @param viewer The player whom to send the menu
     * @param block  Block which has triggered the opening, can be null
     */
    public void openHubMenuFor(Player viewer, @Nullable Block block) {
        new EnderChestHubMenu(this).open(viewer);

        if (block != null && Files.getConfiguration().isGlobalSound()) {
            MiscUtil.playSound(block.getLocation(), "CHEST_OPEN", "BLOCK_CHEST_OPEN");
        } else {
            MiscUtil.playSound(viewer, "CHEST_OPEN", "BLOCK_CHEST_OPEN");
        }
    }

    /**
     * Permits to open the Hub menu with the list of enderchests
     * of a specific player to another human.
     *
     * @param viewer The player whom to send the menu
     */
    public void openHubMenuFor(Player viewer) {
        this.openHubMenuFor(viewer, null);
    }

    /**
     * Permits to open an enderchest to a viewer.
     *
     * @param viewer The viewer for which the enderchest will be displayed
     * @param num    The number of the enderchest to open
     * @return true if the enderchest has been opened
     */
    public boolean openEnderchestFor(Player viewer, int num) {
        Optional<EnderChest> chest = this.getChest(num);
        boolean accessible = false;

        if (chest.isPresent()) {
            accessible = chest.get().isAccessible();

            if (accessible) {
                chest.get().openContainerFor(viewer);
            }
        }

        return accessible;
    }

    /**
     * Count the number of accessible enderchests of the owner.
     *
     * @return The number of accessible enderchests loaded in the context
     */
    public int getAccessibleChestCount() {
        return (int) this.chests.stream().filter(EnderChest::isAccessible).count();
    }

}

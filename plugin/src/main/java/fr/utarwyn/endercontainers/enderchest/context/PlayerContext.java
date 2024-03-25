package fr.utarwyn.endercontainers.enderchest.context;

import fr.utarwyn.endercontainers.Managers;
import fr.utarwyn.endercontainers.compatibility.CompatibilityHelper;
import fr.utarwyn.endercontainers.configuration.Files;
import fr.utarwyn.endercontainers.enderchest.EnderChest;
import fr.utarwyn.endercontainers.enderchest.VanillaEnderChest;
import fr.utarwyn.endercontainers.inventory.menu.EnderChestListMenu;
import fr.utarwyn.endercontainers.storage.StorageManager;
import fr.utarwyn.endercontainers.storage.player.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.*;

/**
 * A context in which all enderchests of a player are loaded.
 *
 * @author Utarwyn
 * @since 2.2.0
 */
public class PlayerContext {

    /**
     * Owner of this memory context
     */
    private final UUID owner;

    /**
     * Storage object which manages data of the player
     */
    private final PlayerData data;

    /**
     * List of all chests loaded in the context
     */
    private final Set<EnderChest> chests;

    /**
     * Construct a new player context.
     *
     * @param owner owner of the context
     */
    PlayerContext(UUID owner) {
        this.owner = owner;
        this.chests = Collections.synchronizedSet(new HashSet<>());
        this.data = Managers.get(StorageManager.class).createPlayerDataStorage(this.owner);
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
     * Get the owner as a Player object.
     * The player must be online to get the object.
     *
     * @return player object of the owner if it is connected, null otherwise
     */
    public Player getOwnerAsObject() {
        Player player = Bukkit.getPlayer(this.getOwner());
        return player != null && player.isOnline() ? player : null;
    }

    /**
     * Get the storage object which manages this context.
     *
     * @return this storage object
     */
    public PlayerData getData() {
        return this.data;
    }

    /**
     * Searching in the context for a chest which has a specific number.
     *
     * @param num The number of the chest
     * @return chest found
     */
    public Optional<EnderChest> getChest(int num) {
        return this.chests.stream()
                .filter(ch -> ch.getNum() == num)
                .findFirst();
    }

    /**
     * Count the number of accessible enderchests of the owner.
     *
     * @return The number of accessible enderchests loaded in the context
     */
    public int getAccessibleChestCount() {
        return (int) this.chests.stream()
                .filter(EnderChest::isAccessible)
                .count();
    }

    /**
     * Check if there is no player using containers of this context.
     *
     * @return true if chests of this context are unused
     */
    public boolean isChestsUnused() {
        return this.chests.stream().noneMatch(EnderChest::isContainerUsed);
    }

    /**
     * Loads a certain amount of enderchests in this player context.
     *
     * @param count amount of chests to load
     */
    public void loadEnderchests(int count) {
        this.chests.clear();
        for (int i = 0; i < count; i++) {
            this.chests.add(this.createEnderchest(i));
        }
    }

    /**
     * Loads offline player profile if needed for its vanilla enderchest.
     */
    public void loadOfflinePlayerProfile() throws PlayerOfflineLoadException {
        if (Files.getConfiguration().isUseVanillaEnderchest() && this.getOwnerAsObject() == null) {
            Optional<EnderChest> chest = this.getChest(0);
            if (chest.isPresent() && chest.get() instanceof VanillaEnderChest) {
                ((VanillaEnderChest) chest.get()).loadOfflinePlayer();
            }
        }
    }

    /**
     * Permits to open the inventory with all enderchests
     * of a specific player to another human.
     *
     * @param viewer player who want to open the inventory
     * @param block  block which has triggered the opening, can be null
     */
    public void openListInventory(Player viewer, Block block) {
        if (this.getAccessibleChestCount() == 1 && Files.getConfiguration().isOnlyShowAccessibleEnderchests()) {
            this.openEnderchestInventory(viewer, 0);
        } else {
            new EnderChestListMenu(this).open(viewer);
        }

        Sound sound = CompatibilityHelper.searchSound("CHEST_OPEN", "BLOCK_CHEST_OPEN");
        if (block != null && Files.getConfiguration().isGlobalSound()) {
            block.getWorld().playSound(block.getLocation(), sound, 1f, 1f);
        } else {
            viewer.playSound(viewer.getLocation(), sound, 1f, 1f);
        }
    }

    /**
     * Permits to open the inventory with all enderchests
     * of a specific player to another human.
     *
     * @param viewer player who want to open the inventory
     */
    public void openListInventory(Player viewer) {
        this.openListInventory(viewer, null);
    }

    /**
     * Permits to open an enderchest inventory to a viewer.
     *
     * @param viewer viewer who want to open the enderchest inventory
     * @param num    number of the enderchest to open
     * @return true if the enderchest has been opened
     */
    public boolean openEnderchestInventory(Player viewer, int num) {
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
     * Update container of all chests.
     */
    public void update() {
        this.chests.forEach(EnderChest::updateContainer);
    }

    /**
     * Save all datas stored in the context.
     */
    public void save() {
        this.data.saveContext(this.chests);
    }

    /**
     * Create an object to manage an enderchest.
     *
     * @param number number of the chest
     * @return created enderchest instance
     */
    private EnderChest createEnderchest(int number) {
        if (number == 0 && Files.getConfiguration().isUseVanillaEnderchest()) {
            return new VanillaEnderChest(this);
        } else {
            return new EnderChest(this, number);
        }
    }

}

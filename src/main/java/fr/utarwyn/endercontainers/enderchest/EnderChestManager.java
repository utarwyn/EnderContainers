package fr.utarwyn.endercontainers.enderchest;

import fr.utarwyn.endercontainers.AbstractManager;
import fr.utarwyn.endercontainers.Managers;
import fr.utarwyn.endercontainers.configuration.Files;
import fr.utarwyn.endercontainers.enderchest.context.LoadTask;
import fr.utarwyn.endercontainers.enderchest.context.PlayerContext;
import fr.utarwyn.endercontainers.enderchest.context.SaveTask;
import fr.utarwyn.endercontainers.menu.MenuManager;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * The new enderchest manager to manage all chests
 *
 * @author Utarwyn
 * @since 2.0.0
 */
public class EnderChestManager extends AbstractManager {

    /**
     * A map which contains all loaded player contexts.
     */
    Map<UUID, PlayerContext> contextMap;

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize() {
        this.registerListener(new EnderChestListener(this));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void load() {
        this.contextMap = new ConcurrentHashMap<>();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected synchronized void unload() {
        // Close all menus
        this.plugin.executeTaskOnMainThread(() -> Managers.get(MenuManager.class).closeAll());

        // Save and unload all data
        this.contextMap.values().forEach(context -> new SaveTask(context).run());
        this.contextMap.clear();
    }

    /**
     * Get from configuration the count of enderchests a player can have.
     *
     * @return maximum number of enderchests
     */
    public int getMaxEnderchests() {
        return Files.getConfiguration().getMaxEnderchests();
    }

    /**
     * Retrieve the vanilla enderchest used by a specific player.
     * It searchs for the enderchest through all contexts, so it can be slow.
     *
     * @param player viewer of the enderchest
     * @return found vanilla enderchest, null otherwise
     */
    public Optional<VanillaEnderChest> getVanillaEnderchestUsedBy(Player player) {
        if (!Files.getConfiguration().isUseVanillaEnderchest()) {
            return Optional.empty();
        }

        return this.contextMap.values().stream()
                .map(context -> context.getChest(0))
                .filter(Optional::isPresent)
                .map(ec -> (VanillaEnderChest) ec.get())
                .filter(ec -> ec.isUsedBy(player))
                .findFirst();
    }

    /**
     * Check if the context of a specific player is unused at a given time.
     * All chests of this context must not have viewer in their container.
     *
     * @param owner owner of the context to check
     * @return true if the context is unused
     */
    public boolean isContextUnused(UUID owner) {
        return this.contextMap.containsKey(owner) && this.contextMap.get(owner).isChestsUnused();
    }

    /**
     * Load all chests and data of a player asynchronously if needed
     * and call a method when this work is done.
     *
     * @param owner    player for which the method has to load context
     * @param consumer method consumed at the end of the task
     */
    public void loadPlayerContext(UUID owner, Consumer<PlayerContext> consumer) {
        if (!this.contextMap.containsKey(owner)) {
            this.plugin.getServer().getScheduler().runTaskAsynchronously(this.plugin,
                    new LoadTask(this.plugin, this, owner, consumer));
        } else {
            consumer.accept(this.contextMap.get(owner));
        }
    }

    /**
     * Register a context of loaded enderchests for a specific player.
     *
     * @param context context to register
     */
    public void registerPlayerContext(PlayerContext context) {
        this.contextMap.put(context.getOwner(), context);
    }

    /**
     * Save all data of a player.
     * Also purge its context from memory if needed.
     *
     * @param owner  owner of the player context to save
     * @param delete should the context must be deleted from the memory
     */
    public void savePlayerContext(UUID owner, boolean delete) {
        if (this.contextMap.containsKey(owner)) {
            SaveTask saveTask = new SaveTask(this.contextMap.get(owner));
            this.plugin.getServer().getScheduler().runTaskAsynchronously(this.plugin, saveTask);

            if (delete) {
                this.contextMap.remove(owner);
            }
        }
    }

}

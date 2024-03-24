package fr.utarwyn.endercontainers.enderchest;

import fr.utarwyn.endercontainers.AbstractManager;
import fr.utarwyn.endercontainers.Managers;
import fr.utarwyn.endercontainers.configuration.Files;
import fr.utarwyn.endercontainers.enderchest.context.LoadTask;
import fr.utarwyn.endercontainers.enderchest.context.PlayerContext;
import fr.utarwyn.endercontainers.enderchest.context.SaveTask;
import fr.utarwyn.endercontainers.enderchest.listener.EnderChestInventoryListener;
import fr.utarwyn.endercontainers.enderchest.listener.EnderChestListener;
import fr.utarwyn.endercontainers.inventory.InventoryManager;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
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
     * Collection of player identifiers which are waiting for context loading
     */
    Set<UUID> loadingContexts;

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize() {
        this.registerListener(new EnderChestListener(this));
        this.registerListener(new EnderChestInventoryListener(this));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void load() {
        this.contextMap = new ConcurrentHashMap<>();
        this.loadingContexts = ConcurrentHashMap.newKeySet();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected synchronized void unload() {
        // Close all inventories
        this.plugin.executeTaskOnMainThread(() -> Managers.get(InventoryManager.class).closeAll());

        // Save and unload all data
        this.loadingContexts.clear();
        this.contextMap.values().forEach(PlayerContext::update);
        this.contextMap.values().forEach(PlayerContext::save);
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
     * Get the all context of loaded players.
     *
     * @return context map of all players
     */
    public Map<UUID, PlayerContext> getContextMap() {
        return contextMap;
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
     * Loads data context of a player asynchronously if needed and consume it when done.
     * Uses a temporary collection to avoid duplication loading.
     *
     * @param owner    player for which the method has to load context
     * @param consumer method consumed at the end of the task
     */
    public void loadPlayerContext(UUID owner, Consumer<PlayerContext> consumer) {
        if (!this.loadingContexts.contains(owner)) {
            if (this.contextMap.containsKey(owner)) {
                consumer.accept(this.contextMap.get(owner));
            } else {
                this.loadingContexts.add(owner);
                this.plugin.getServer().getScheduler().runTaskAsynchronously(this.plugin,
                        new LoadTask(this.plugin, this, owner, consumer));
            }
        }
    }

    /**
     * Registers a context of loaded enderchests for a specific player.
     * Also unsets context from loading state.
     *
     * @param context context to register
     */
    public void registerPlayerContext(PlayerContext context) {
        this.contextMap.put(context.getOwner(), context);
        this.loadingContexts.remove(context.getOwner());
    }

    /**
     * Save all data of a player.
     *
     * @param owner owner of the player context to save
     */
    public void savePlayerContext(UUID owner) {
        if (this.contextMap.containsKey(owner)) {
            PlayerContext playerContext = this.contextMap.get(owner);
            SaveTask saveTask = new SaveTask(playerContext);

            playerContext.update();
            this.plugin.executeTaskOnOtherThread(saveTask);
        }
    }

    /**
     * Delete a player context from memory if it is unused.
     *
     * @param owner owner of the player context
     */
    public void deletePlayerContextIfUnused(UUID owner) {
        if (this.isContextUnused(owner)) {
            this.contextMap.remove(owner);
        }
    }

}

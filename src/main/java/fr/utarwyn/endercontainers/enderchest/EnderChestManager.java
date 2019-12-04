package fr.utarwyn.endercontainers.enderchest;

import fr.utarwyn.endercontainers.AbstractManager;
import fr.utarwyn.endercontainers.Managers;
import fr.utarwyn.endercontainers.configuration.Files;
import fr.utarwyn.endercontainers.enderchest.context.ContextRunnable;
import fr.utarwyn.endercontainers.enderchest.context.LoadTask;
import fr.utarwyn.endercontainers.enderchest.context.PlayerContext;
import fr.utarwyn.endercontainers.menu.MenuManager;
import fr.utarwyn.endercontainers.storage.StorageWrapper;
import fr.utarwyn.endercontainers.storage.player.PlayerData;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

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
    private Map<UUID, PlayerContext> contextMap;

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
    public void load() {
        this.contextMap = new ConcurrentHashMap<>();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void unload() {
        // Close all menus
        Managers.get(MenuManager.class).closeAll();

        // Unload all data
        this.contextMap.clear();
        StorageWrapper.unload(PlayerData.class);
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
     * Load all chests and data of a player asynchronously if needed
     * and call a method when this work is done.
     *
     * @param owner    player for which the method has to load context
     * @param callback method called at the end of the task
     */
    public void loadPlayerContext(UUID owner, ContextRunnable callback) {
        if (!this.contextMap.containsKey(owner)) {
            this.plugin.getServer().getScheduler().runTaskAsynchronously(this.plugin,
                    new LoadTask(this.plugin, this, owner, callback));
        } else {
            callback.run(this.contextMap.get(owner));
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
     * Purge all chests and the context of a player from memory
     */
    void deletePlayerContext(UUID owner) {
        this.contextMap.remove(owner);
    }

}

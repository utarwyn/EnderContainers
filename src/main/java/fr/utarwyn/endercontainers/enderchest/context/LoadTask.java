package fr.utarwyn.endercontainers.enderchest.context;

import fr.utarwyn.endercontainers.EnderContainers;
import fr.utarwyn.endercontainers.enderchest.EnderChestManager;

import java.util.UUID;

/**
 * Represents the task which loads a memory context with
 * enderchests data of a specific player.
 *
 * @author Utarwyn
 * @since 2.3.0
 */
public class LoadTask implements Runnable {

    /**
     * The EnderContainers plugin
     */
    private EnderContainers plugin;

    /**
     * The Enderchest manager
     */
    private EnderChestManager manager;

    /**
     * Owner for who we have to create a context
     */
    private UUID owner;

    /**
     * Entity to run when this task is finished
     */
    private ContextRunnable runnable;

    /**
     * Construct a new loading task.
     *
     * @param plugin   the main plugin
     * @param manager  the enderchests manager
     * @param owner    owner of the context to load
     * @param runnable a callback entity
     */
    public LoadTask(EnderContainers plugin, EnderChestManager manager, UUID owner, ContextRunnable runnable) {
        this.plugin = plugin;
        this.manager = manager;
        this.owner = owner;
        this.runnable = runnable;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void run() {
        // Initialize the player context
        int count = this.manager.getMaxEnderchests();
        PlayerContext context = new PlayerContext(this.owner);

        // This task can take a certain amount of time to be executed
        context.loadEnderchests(count);

        // Schedule the callback and register the player context in memory
        this.plugin.getServer().getScheduler().scheduleSyncDelayedTask(this.plugin, () -> {
            this.manager.registerPlayerContext(context);
            this.runnable.run(context);
        });
    }

}

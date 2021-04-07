package fr.utarwyn.endercontainers.enderchest.context;

import fr.utarwyn.endercontainers.EnderContainers;
import fr.utarwyn.endercontainers.enderchest.EnderChestManager;

import java.util.UUID;
import java.util.function.Consumer;

/**
 * Represents the task which loads a memory context with
 * enderchests data of a specific player.
 *
 * @author Utarwyn
 * @since 2.2.0
 */
public class LoadTask implements Runnable {

    /**
     * The EnderContainers plugin
     */
    private final EnderContainers plugin;

    /**
     * The Enderchest manager
     */
    private final EnderChestManager manager;

    /**
     * Owner for who we have to create a context
     */
    private final UUID owner;

    /**
     * Object to consum when this task is finished
     */
    private final Consumer<PlayerContext> consumer;

    /**
     * Construct a new loading task.
     *
     * @param plugin   the main plugin
     * @param manager  the enderchests manager
     * @param owner    owner of the context to load
     * @param consumer object to consum at the end of the task
     */
    public LoadTask(EnderContainers plugin, EnderChestManager manager, UUID owner, Consumer<PlayerContext> consumer) {
        this.plugin = plugin;
        this.manager = manager;
        this.owner = owner;
        this.consumer = consumer;
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

        this.plugin.getServer().getScheduler().scheduleSyncDelayedTask(this.plugin, () -> {
            // Load offline player profile in a synchronous way if needed
            context.loadOfflinePlayerProfile();

            // Register the player context in memory
            this.manager.registerPlayerContext(context);
            this.consumer.accept(context);
        });
    }

}

package fr.utarwyn.endercontainers.enderchest.context;

/**
 * Represents the task which saves in a persistant storage all data
 * of the context of a specific player.
 *
 * @author Utarwyn
 * @since 2.2.0
 */
public class SaveTask implements Runnable {

    /**
     * The player context to save
     */
    private final PlayerContext context;

    /**
     * Construct a new saving task.
     *
     * @param context the player context to save
     */
    public SaveTask(PlayerContext context) {
        this.context = context;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void run() {
        this.context.save();
    }

}

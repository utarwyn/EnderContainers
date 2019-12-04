package fr.utarwyn.endercontainers.enderchest.context;

/**
 * Represents an entity which can be used to
 * perform some actions on a loaded player context.
 *
 * @author Utarwyn
 * @since 2.3.0
 */
public interface ContextRunnable {

    /**
     * Method called when the context is loaded.
     *
     * @param context loaded context
     */
    void run(PlayerContext context);

}

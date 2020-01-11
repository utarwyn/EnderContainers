package fr.utarwyn.endercontainers.backup.action;

/**
 * Represents a callback which is called when
 * an action regarding backups is finished.
 *
 * @author Utarwyn
 * @since 2.3.0
 */
public interface ActionCallback {

    /**
     * Method called when the action is finished.
     *
     * @param result result of the action
     */
    void run(boolean result);

}

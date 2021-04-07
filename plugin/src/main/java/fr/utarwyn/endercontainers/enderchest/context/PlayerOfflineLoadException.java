package fr.utarwyn.endercontainers.enderchest.context;

/**
 * Thrown when an error occured during offline player profile loading.
 *
 * @author Utarwyn
 * @since 2.3.0
 */
public class PlayerOfflineLoadException extends Exception {

    /**
     * Constructs an exception.
     *
     * @param message message with details of the error
     * @param cause   element which causes the error
     */
    public PlayerOfflineLoadException(String message, Throwable cause) {
        super(message, cause);
    }

}

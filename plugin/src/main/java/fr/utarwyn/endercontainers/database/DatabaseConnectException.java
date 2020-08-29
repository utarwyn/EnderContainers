package fr.utarwyn.endercontainers.database;

/**
 * Handles database connection exceptions.
 *
 * @author Utarwyn
 * @since 2.2.1
 */
public class DatabaseConnectException extends Exception {

    /**
     * Construct an exception with a parent cause.
     *
     * @param cause cause which triggered this exception
     */
    public DatabaseConnectException(Throwable cause) {
        super(cause);
    }

}

package fr.utarwyn.endercontainers.configuration;

/**
 * Thrown if an error occured during
 * the loading of a configuration file.
 *
 * @author Utarwyn
 * @since 2.3.0
 */
public class ConfigLoadingException extends Exception {

    /**
     * Constructs the config file loading exception.
     *
     * @param message message to attach to the exception
     */
    public ConfigLoadingException(String message) {
        super(message);
    }

    /**
     * Constructs the config file loading exception.
     *
     * @param message message to attach to the exception
     * @param cause   parent cause which thrown the exception
     */
    public ConfigLoadingException(String message, Throwable cause) {
        super(message, cause);
    }

}

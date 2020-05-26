package fr.utarwyn.endercontainers.configuration.wrapper;

/**
 * Thrown if an error occured during the loading of a configuration file.
 * Needs to be thrown with a message and a parent cause (IO or Reflection).
 *
 * @author Utarwyn <maximemalgorn@gmail.com>
 * @since 2.2.0
 */
public class YamlFileLoadException extends Exception {

    /**
     * Constructs the Yaml file load exception.
     *
     * @param message message to attach to the exception
     * @param cause   parent cause which thrown the exception
     */
    public YamlFileLoadException(String message, Throwable cause) {
        super(message, cause);
    }

}

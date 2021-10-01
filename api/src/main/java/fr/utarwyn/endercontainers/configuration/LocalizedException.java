package fr.utarwyn.endercontainers.configuration;

import java.util.Map;

/**
 * Generic exception with a message from the locale file.
 *
 * @author Utarwyn
 * @since 2.2.0
 */
public class LocalizedException extends Exception {

    /**
     * Locale key
     */
    private final LocaleKey key;

    /**
     * Parameters to replace in the message
     */
    private final Map<String, String> parameters;

    /**
     * Construct the exception with a key.
     *
     * @param key key to find the localized message
     */
    public LocalizedException(LocaleKey key) {
        this(key, null);
    }

    /**
     * Construct the exception with a key.
     *
     * @param key        key to find the localized message
     * @param parameters parameters to replace in the message
     */
    public LocalizedException(LocaleKey key, Map<String, String> parameters) {
        this.key = key;
        this.parameters = parameters;
    }

    /**
     * Get the localized key to display a message for an entity.
     *
     * @return localized key
     */
    public LocaleKey getKey() {
        return key;
    }

    /**
     * Get the parameters to replace in the message.
     *
     * @return message parameters
     */
    public Map<String, String> getParameters() {
        return this.parameters;
    }

}

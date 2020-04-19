package fr.utarwyn.endercontainers.dependency.exceptions;

import fr.utarwyn.endercontainers.LocalizedException;
import fr.utarwyn.endercontainers.configuration.LocaleKey;

import java.util.Map;

/**
 * Exception when a player cannot open
 * an enderchest when using a block.
 *
 * @author Utarwyn <maxime.malgorn@laposte.net>
 * @since 2.2.0
 */
public class BlockChestOpeningException extends LocalizedException {

    public BlockChestOpeningException() {
        super(null);
    }

    public BlockChestOpeningException(LocaleKey key) {
        super(key);
    }

    public BlockChestOpeningException(LocaleKey key, Map<String, String> parameters) {
        super(key, parameters);
    }

}

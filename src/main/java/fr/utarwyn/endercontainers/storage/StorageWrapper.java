package fr.utarwyn.endercontainers.storage;

import java.util.logging.Logger;

/**
 * Allows to manage storage using different technologies with same methods.
 * (for exemple MySQL and flatfile)
 *
 * @author Utarwyn
 * @since 2.0.0
 */
public abstract class StorageWrapper {

    /**
     * The plugin logger
     */
    protected Logger logger;

    /**
     * Construct a new storage wrapper.
     *
     * @param logger plugin logger
     */
    public StorageWrapper(Logger logger) {
        this.logger = logger;
    }

    /**
     * Called when a storage needs to load something
     */
    protected abstract void load();

    /**
     * Called when a storage needs to save something
     */
    protected abstract void save();

}

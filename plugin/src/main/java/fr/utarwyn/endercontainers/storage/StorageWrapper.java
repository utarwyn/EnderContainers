package fr.utarwyn.endercontainers.storage;

import fr.utarwyn.endercontainers.EnderContainers;

/**
 * Allows to manage storage using different technologies with same methods.
 * (for exemple MySQL and flatfile)
 *
 * @author Utarwyn
 * @since 2.0.0
 */
public abstract class StorageWrapper {

    /**
     * The plugin instance
     */
    protected EnderContainers plugin;

    /**
     * Construct a new storage wrapper.
     *
     * @param plugin plugin instance object
     */
    public StorageWrapper(EnderContainers plugin) {
        this.plugin = plugin;
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

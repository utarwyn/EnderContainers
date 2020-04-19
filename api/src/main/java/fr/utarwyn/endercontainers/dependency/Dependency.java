package fr.utarwyn.endercontainers.dependency;

import org.bukkit.plugin.Plugin;

/**
 * Interface Dependency.
 *
 * @author Utarwyn <maxime.malgorn@laposte.net>
 * @since 2.2.0
 */
public interface Dependency extends DependencyValidator {

    /**
     * Called when the dependency has been enabled.
     *
     * @param plugin plugin object of the dependency
     */
    void onEnable(Plugin plugin);

    /**
     * Called when the dependency has been disabled.
     */
    void onDisable();

}

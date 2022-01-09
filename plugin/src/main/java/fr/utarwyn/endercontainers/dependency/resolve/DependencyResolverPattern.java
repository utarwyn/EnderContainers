package fr.utarwyn.endercontainers.dependency.resolve;

import fr.utarwyn.endercontainers.dependency.Dependency;
import org.bukkit.plugin.Plugin;

/**
 * Represents a custom matching
 * pattern used to resolve a dependency.
 *
 * @author Utarwyn
 * @since 2.2.3
 */
public abstract class DependencyResolverPattern {

    private final Class<? extends Dependency> clazz;

    protected DependencyResolverPattern(Class<? extends Dependency> clazz) {
        this.clazz = clazz;
    }

    /**
     * Checks if a plugin matches with stored resolving pattern.
     *
     * @param plugin plugin to check
     * @return true if plugin matches this pattern
     */
    public abstract boolean matchWith(Plugin plugin);

    /**
     * Constructs an instance of registered dependency class for this matcher.
     *
     * @param plugin plugin managed by the dependency
     * @return constructed dependency instance
     */
    public Dependency construct(Plugin plugin) {
        try {
            return this.clazz.getDeclaredConstructor(Plugin.class).newInstance(plugin);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("cannot instanciate dependency class", e);
        }
    }

}

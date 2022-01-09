package fr.utarwyn.endercontainers.dependency.resolve;

import fr.utarwyn.endercontainers.dependency.Dependency;
import org.bukkit.plugin.Plugin;

/**
 * Represents a dependency resolver using an author pattern.
 *
 * @author Utarwyn
 * @since 2.2.3
 */
public class DependencyResolverPatternAuthor extends DependencyResolverPattern {

    private final String author;

    public DependencyResolverPatternAuthor(String author, Class<? extends Dependency> clazz) {
        super(clazz);
        this.author = author;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean matchWith(Plugin plugin) {
        return plugin.getDescription().getAuthors().contains(this.author);
    }

}

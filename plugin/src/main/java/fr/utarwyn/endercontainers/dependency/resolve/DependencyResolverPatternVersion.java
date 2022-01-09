package fr.utarwyn.endercontainers.dependency.resolve;

import fr.utarwyn.endercontainers.dependency.Dependency;
import org.bukkit.plugin.Plugin;

import java.util.regex.Pattern;

/**
 * Represents a dependency resolver using a version pattern.
 *
 * @author Utarwyn
 * @since 2.2.3
 */
public class DependencyResolverPatternVersion extends DependencyResolverPattern {

    private final Pattern pattern;

    public DependencyResolverPatternVersion(String expression, Class<? extends Dependency> clazz) {
        super(clazz);
        this.pattern = Pattern.compile(expression);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean matchWith(Plugin plugin) {
        return this.pattern.matcher(plugin.getDescription().getVersion()).find();
    }

}

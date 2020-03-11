package fr.utarwyn.endercontainers.dependency;

import fr.utarwyn.endercontainers.api.dependency.dependency.Dependency;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Build a new dependency instance with a custom version matching.
 *
 * @author Utarwyn <maxime.malgorn@laposte.net>
 * @since 2.2.0
 */
public class DependencyBuilder {

    /**
     * Bukkit plugin manager
     */
    private PluginManager pluginManager;

    /**
     * Name of the dependency instance to build
     */
    private String name;

    /**
     * Patterns to match dependency's versions
     */
    private Map<Pattern, Class<? extends Dependency>> patterns;

    /**
     * Construct a new dependency builder.
     *
     * @param pluginManager Bukkit plugin manager
     */
    DependencyBuilder(PluginManager pluginManager) {
        this.pluginManager = pluginManager;
        this.patterns = new HashMap<>();
    }

    /**
     * Set the name of the built dependency.
     * Should match the plugin exact name.
     *
     * @param name name of the dependency
     * @return this instance
     */
    public DependencyBuilder name(String name) {
        this.name = name;
        return this;
    }

    /**
     * Match a specific dependency class from a custom version pattern.
     *
     * @param expression pattern to match the dependency's version
     * @param clazz      class to use for for the targeted version
     * @return this instance
     */
    public DependencyBuilder matchVersion(String expression, Class<? extends Dependency> clazz) {
        this.patterns.put(Pattern.compile(expression), clazz);
        return this;
    }

    /**
     * Use a dependency class for any versions of the plugin.
     *
     * @param clazz class to use for with all versions
     * @return this instance
     */
    public DependencyBuilder use(Class<? extends Dependency> clazz) {
        return this.matchVersion(".*", clazz);
    }

    /**
     * Build the dependency if it is loaded and a matcher
     * has targeted the plugin's version from the plugin manager.
     *
     * @return built dependency instance if present
     * @throws ReflectiveOperationException thrown if the dependency instance cannot be created
     */
    public Optional<Dependency> build() throws ReflectiveOperationException {
        if (this.name == null) {
            throw new NullPointerException("Dependency name cannot be null!");
        }
        if (this.patterns.isEmpty()) {
            throw new NullPointerException("A builder must have at least one matcher!");
        }

        if (this.pluginManager.isPluginEnabled(this.name)) {
            Plugin plugin = this.pluginManager.getPlugin(this.name);
            String pluginVersion = plugin != null ? plugin.getDescription().getVersion() : null;
            Optional<Dependency> dependency = this.constructInstance(plugin, pluginVersion);

            dependency.ifPresent(Dependency::onEnable);
            return dependency;
        }

        return Optional.empty();
    }

    /**
     * Construct a dependency instance from patterns and version of the loaded plugin.
     *
     * @param plugin        loaded plugin
     * @param pluginVersion version of the loaded plugin
     * @return dependency instance if present
     * @throws ReflectiveOperationException thrown if the dependency instance cannot be created
     */
    private Optional<Dependency> constructInstance(Plugin plugin, String pluginVersion)
            throws ReflectiveOperationException {
        Optional<? extends Class<? extends Dependency>> foundClazz = this.patterns.entrySet().stream()
                .filter(entry -> entry.getKey().matcher(pluginVersion).find())
                .map(Map.Entry::getValue)
                .findFirst();

        if (foundClazz.isPresent()) {
            Constructor<? extends Dependency> constructor = foundClazz.get().getDeclaredConstructor(String.class, Plugin.class);
            return Optional.of(constructor.newInstance(this.name, plugin));
        }

        return Optional.empty();
    }

}

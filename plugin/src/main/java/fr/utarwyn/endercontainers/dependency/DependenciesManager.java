package fr.utarwyn.endercontainers.dependency;

import fr.utarwyn.endercontainers.AbstractManager;
import fr.utarwyn.endercontainers.dependency.exceptions.BlockChestOpeningException;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;

/**
 * Class which manage all the dependencies of the plugin.
 *
 * @author Utarwyn
 * @since 1.0.3
 */
public class DependenciesManager extends AbstractManager implements DependencyValidator {

    /**
     * The Bukkit plugin manager
     */
    private PluginManager pluginManager;

    /**
     * A list of all loaded dependencies
     */
    private final Set<Dependency> dependencies;

    /**
     * Construct the dependencies manager.
     */
    public DependenciesManager() {
        this.dependencies = new HashSet<>();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void load() {
        this.pluginManager = this.plugin.getServer().getPluginManager();

        this.loadDependencies();
        this.logDependencies();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void unload() {
        this.dependencies.forEach(Dependency::onDisable);
        this.dependencies.clear();
    }

    /**
     * Retrieve all loaded dependencies.
     *
     * @return list of all dependencies
     */
    public Set<Dependency> getDependencies() {
        return this.dependencies;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void validateBlockChestOpening(Block block, Player player)
            throws BlockChestOpeningException {
        for (Dependency dependency : this.dependencies) {
            dependency.validateBlockChestOpening(block, player);
        }
    }

    /**
     * Load each dependency if the needed plugin is enabled.
     */
    private void loadDependencies() {
        // Essentials
        new DependencyResolver(this.pluginManager)
                .name("Essentials")
                .use(EssentialsDependency.class)
                .resolve().ifPresent(this::enableDependency);

        // Factions
        new DependencyResolver(this.pluginManager)
                .name("Factions")
                .matchVersion("^1\\.6.*", Factions1Dependency.class)
                .matchVersion("^2.*", Factions2Dependency.class)
                .resolve().ifPresent(this::enableDependency);

        // PlotSquared
        new DependencyResolver(this.pluginManager)
                .name("PlotSquared")
                .matchVersion("^5.*", PlotSquaredDependency.class)
                .resolve().ifPresent(this::enableDependency);

        // WorldGuard
        new DependencyResolver(this.pluginManager)
                .name("WorldGuard")
                .matchVersion("^6.*", WorldGuard6Dependency.class)
                .matchVersion("^7.*", WorldGuard7Dependency.class)
                .resolve().ifPresent(this::enableDependency);
    }

    /**
     * Logs all dependencies in the console.
     */
    private void logDependencies() {
        this.logger.info("-----------[Dependencies]-----------");

        this.dependencies.stream().map(Dependency::getPlugin).forEach(plugin ->
                this.logger.log(Level.INFO, "  Use {0} (v{1}) as a dependency!",
                        new Object[]{plugin.getName(), plugin.getDescription().getVersion()})
        );

        int size = this.dependencies.size();
        if (size > 0) {
            String plural = size > 1 ? "ies" : "y";
            this.logger.log(Level.INFO, "  {0} dependenc{1} loaded!", new Object[]{size, plural});
        } else {
            this.logger.info("  No dependency found.");
        }

        this.logger.info("------------------------------------");
    }

    /**
     * Load and enable a dependency from its resolved info.
     *
     * @param dependency resolved dependency
     */
    private void enableDependency(Dependency dependency) {
        dependency.onEnable();
        this.dependencies.add(dependency);
    }

}

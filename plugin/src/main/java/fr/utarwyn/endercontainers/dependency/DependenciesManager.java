package fr.utarwyn.endercontainers.dependency;

import fr.utarwyn.endercontainers.AbstractManager;
import fr.utarwyn.endercontainers.dependency.exceptions.BlockChestOpeningException;
import fr.utarwyn.endercontainers.dependency.resolver.DependencyInfo;
import fr.utarwyn.endercontainers.dependency.resolver.DependencyResolver;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;

import java.util.ArrayList;
import java.util.List;
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
    private List<Dependency> dependencies;

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void load() {
        this.dependencies = new ArrayList<>();
        this.pluginManager = this.plugin.getServer().getPluginManager();
        this.loadDependenciesWithLog();
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
                .use(PlotSquaredDependency.class)
                .resolve().ifPresent(this::enableDependency);

        // WorldGuard
        new DependencyResolver(this.pluginManager)
                .name("WorldGuard")
                .matchVersion("^6.*", WorldGuard6Dependency.class)
                .matchVersion("^7.*", WorldGuard7Dependency.class)
                .resolve().ifPresent(this::enableDependency);
    }

    /**
     * Load each dependency if the needed plugin is enabled and logs it.
     */
    private void loadDependenciesWithLog() {
        this.logger.info("-----------[Dependencies]-----------");

        this.loadDependencies();

        int size = this.dependencies.size();
        if (size > 0) {
            this.logger.log(Level.INFO, "  {0} dependenc{1} loaded!", new Object[]{size, (size > 1 ? "ies" : "y")});
        } else {
            this.logger.info("  No dependency found.");
        }

        this.logger.info("------------------------------------");
    }

    /**
     * Load and enable a dependency from its resolved info.
     *
     * @param info resolved dependency info
     */
    private void enableDependency(DependencyInfo info) {
        this.logger.log(Level.INFO, "  Use {0} (v{1}) as a dependency!",
                new Object[]{info.getPlugin().getName(), info.getPluginVersion()});

        info.getDependency().onEnable(info.getPlugin());
        this.dependencies.add(info.getDependency());
    }

}

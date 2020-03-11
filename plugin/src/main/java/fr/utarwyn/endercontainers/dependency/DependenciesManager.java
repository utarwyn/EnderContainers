package fr.utarwyn.endercontainers.dependency;

import fr.utarwyn.endercontainers.AbstractManager;
import fr.utarwyn.endercontainers.api.dependency.dependency.Dependency;
import fr.utarwyn.endercontainers.api.dependency.dependency.DependencyListener;
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
public class DependenciesManager extends AbstractManager implements DependencyListener {

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

        try {
            this.loadDependencies();
            this.logLoadedDependencies();
        } catch (ReflectiveOperationException e) {
            this.logger.log(Level.SEVERE, "Cannot load one of dependency objects", e);
        }
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
     * Called when a player wants to open its enderchest
     * by interacting with an enderchest block.
     *
     * @param block       block clicked by the player
     * @param player      player who interacts with the chest
     * @param sendMessage send a message to the player if action is forbidden
     * @return true if the block chest can be opened
     */
    @Override
    public boolean onBlockChestOpened(Block block, Player player, boolean sendMessage) {
        return this.dependencies.stream().allMatch(d -> d.onBlockChestOpened(block, player, sendMessage));
    }

    /**
     * Load each dependency if the needed plugin is enabled.
     */
    private void loadDependencies() throws ReflectiveOperationException {
        // Essentials
        new DependencyBuilder(this.pluginManager)
                .name("Essentials")
                .use(EssentialsDependency.class)
                .build().ifPresent(this.dependencies::add);

        // Factions
        new DependencyBuilder(this.pluginManager)
                .name("Factions")
                .matchVersion("^1\\.6.*", Factions1Dependency.class)
                .matchVersion("^2.*", Factions2Dependency.class)
                .build().ifPresent(this.dependencies::add);

        // PlotSquared
        new DependencyBuilder(this.pluginManager)
                .name("PlotSquared")
                .use(PlotSquaredDependency.class)
                .build().ifPresent(this.dependencies::add);

        // WorldGuard
        new DependencyBuilder(this.pluginManager)
                .name("WorldGuard")
                .matchVersion("^6.*", WorldGuard6Dependency.class)
                .matchVersion("^7.*", WorldGuard7Dependency.class)
                .build().ifPresent(this.dependencies::add);
    }

    /**
     * This method logs all information about loaded dependencies.
     */
    private void logLoadedDependencies() {
        this.logger.info("-----------[Dependencies]-----------");

        this.dependencies.forEach(dependency -> this.logger.log(
                Level.INFO, "  Use {0} (v{1}) as a dependency!",
                new Object[]{dependency.getName(), dependency.getPluginVersion()}
        ));

        int size = this.dependencies.size();
        if (size > 0) {
            this.logger.log(Level.INFO, "  {0} dependenc{1} loaded!", new Object[]{size, (size > 1 ? "ies" : "y")});
        } else {
            this.logger.info("  No dependency found.");
        }

        this.logger.info("------------------------------------");
    }

}

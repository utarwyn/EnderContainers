package fr.utarwyn.endercontainers.dependency;

import fr.utarwyn.endercontainers.AbstractManager;
import fr.utarwyn.endercontainers.dependency.faction.FactionsDependency;
import fr.utarwyn.endercontainers.dependency.worldguard.WorldGuardDependency;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    public void load() {
        this.dependencies = new ArrayList<>();
        this.pluginManager = this.plugin.getServer().getPluginManager();

        this.loadDependencies();
        this.logLoadedDependencies();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void unload() {
        for (Dependency dependency : this.dependencies) {
            dependency.onDisable();
        }
        this.dependencies.clear();
    }

    /**
     * Called when a player wants to open its enderchest by interacting with an enderchest block
     * (This method loop loaded dependencies to call the {@link DependencyListener#onBlockChestOpened(Block, Player, boolean)} method on each of them)
     *
     * @param block       The block clicked by the player
     * @param player      The player who interacts with the chest.
     * @param sendMessage The plugin have to send a message to the player.
     * @return True if the block chest can be opened
     */
    @Override
    public boolean onBlockChestOpened(Block block, Player player, boolean sendMessage) {
        for (Dependency dependency : this.dependencies) {
            if (!dependency.onBlockChestOpened(block, player, sendMessage)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Load each dependency if the needed plugin is enabled.
     */
    private void loadDependencies() {
        Map<String, Class<? extends Dependency>> dependencyClasses = new HashMap<>();

        // Prepare all dependencies here
        dependencyClasses.put("Citizens", CitizensDependency.class); // deprecated
        dependencyClasses.put("Essentials", EssentialsDependency.class);
        dependencyClasses.put("Factions", FactionsDependency.class);
        dependencyClasses.put("PlotSquared", PlotSquaredDependency.class);
        dependencyClasses.put("WorldGuard", WorldGuardDependency.class);

        // And register them if the plugin is loaded on the server.
        for (Map.Entry<String, Class<? extends Dependency>> dependency : dependencyClasses.entrySet()) {
            String name = dependency.getKey();

            if (this.pluginManager.isPluginEnabled(name)) {
                try {
                    this.registerDependency(name, dependency.getValue().newInstance());
                } catch (InstantiationException | IllegalAccessException e) {
                    this.logger.log(Level.SEVERE, "Cannot instanciate the dependency " + name, e);
                }
            }
        }
    }

    /**
     * This method logs all information about loaded dependencies.
     */
    private void logLoadedDependencies() {
        this.logger.info("-----------[Dependencies]-----------");

        for (Dependency dependency : this.dependencies) {
            this.logger.log(
                    Level.INFO, "  Use {0} (v{1}) as a dependency!",
                    new Object[]{dependency.getName(), dependency.getPluginVersion()}
            );
        }

        int size = this.dependencies.size();
        if (size > 0) {
            this.logger.log(Level.INFO, "  {0} dependenc{1} loaded!", new Object[]{size, (size > 1 ? "ies" : "y")});
        } else {
            this.logger.info("  No dependency found.");
        }

        this.logger.info("------------------------------------");
    }

    /**
     * Register a dependency, add it to the list and enable it.
     *
     * @param name       Name of the dependency
     * @param dependency Dependency to register
     */
    private void registerDependency(String name, Dependency dependency) {
        dependency.setName(name);
        dependency.setPlugin(this.pluginManager.getPlugin(name));
        dependency.onEnable();
        this.dependencies.add(dependency);
    }

}

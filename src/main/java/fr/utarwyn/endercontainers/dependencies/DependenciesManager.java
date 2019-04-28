package fr.utarwyn.endercontainers.dependencies;

import fr.utarwyn.endercontainers.AbstractManager;
import fr.utarwyn.endercontainers.EnderContainers;
import fr.utarwyn.endercontainers.dependencies.faction.FactionsDependency;
import fr.utarwyn.endercontainers.dependencies.worldguard.WorldGuardDependency;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
	 * Constructor of the class. That's all.
	 */
	public DependenciesManager() {
		super(EnderContainers.getInstance());
	}

	/**
	 * Called when the manager is initializing
	 */
	@Override
	public void load() {
		this.dependencies = new ArrayList<>();
		this.pluginManager = this.plugin.getServer().getPluginManager();

		this.loadDependencies();
		this.logLoadedDependencies();
	}

	/**
	 * Called when the manager is unloading
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
		Map<String, Class<? extends Dependency>> dependencies = new HashMap<>();

		// Prepare all dependencies here
		dependencies.put("Citizens", CitizensDependency.class); // deprecated
		dependencies.put("Essentials", EssentialsDependency.class);
		dependencies.put("Factions", FactionsDependency.class);
		dependencies.put("PlotSquared", PlotSquaredDependency.class);
		dependencies.put("WorldGuard", WorldGuardDependency.class);

		// And register them if the plugin is loaded on the server.
		for (Map.Entry<String, Class<? extends Dependency>> dependency : dependencies.entrySet()) {
			if (this.pluginManager.isPluginEnabled(dependency.getKey())) {
				try {
					this.registerDependency(dependency.getKey(), dependency.getValue().newInstance());
				} catch (InstantiationException | IllegalAccessException e) {
					e.printStackTrace();
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
			this.logger.info("  Use " + dependency.getName() +
					" (v" + dependency.getPluginVersion() + ") as a dependency!");
		}

		int size = this.dependencies.size();
		if (size > 0) {
			this.logger.info("  " + size + " dependenc" + (size > 1 ? "ies" : "y") + " loaded!");
		} else {
			this.logger.info("  No dependency found.");
		}

		this.logger.info("------------------------------------");
	}

	/**
	 * Register a dependency, add it to the list and enable it.
	 * @param name Name of the dependency
	 * @param dependency Dependency to register
	 */
	private void registerDependency(String name, Dependency dependency) {
		dependency.setName(name);
		dependency.setPlugin(this.pluginManager.getPlugin(name));
		dependency.onEnable();
		this.dependencies.add(dependency);
	}

}

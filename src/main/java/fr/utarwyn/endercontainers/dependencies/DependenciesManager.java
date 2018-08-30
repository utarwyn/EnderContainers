package fr.utarwyn.endercontainers.dependencies;

import fr.utarwyn.endercontainers.AbstractManager;
import fr.utarwyn.endercontainers.EnderContainers;
import fr.utarwyn.endercontainers.util.Log;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * Class which manage all the dependencies of the plugin.
 *
 * @author Utarwyn
 * @since 1.0.3
 */
public class DependenciesManager extends AbstractManager implements DependencyListener {

	/**
	 * A field which represents all plugins supported by EnderContainers as dependency.
	 */
	private static final String[] DEPENDENCIES_NAMES = new String[]{
			"Factions", "WorldGuard", "Citizens",
			"PlotSquared", "Essentials"
	};

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
	public void initialize() {
		this.dependencies = new ArrayList<>();

		Log.log("-----------[Dependencies]-----------", true);

		this.loadDependencies();

		int size = this.dependencies.size();
		if (size > 0) {
			Log.log("  " + size + " dependenc" + (size > 1 ? "ies" : "y") + " loaded!", true);
		} else {
			Log.log("  No dependency found.", true);
		}

		Log.log("------------------------------------", true);
	}

	/**
	 * Called when the manager is unloading
	 */
	@Override
	protected void unload() {
		for (Dependency dependency : this.dependencies)
			dependency.onDisable();
	}

	/**
	 * Returns a dependency by its name
	 *
	 * @param dependencyName The name used for the research.
	 * @return The dependency found with the name otherwise null.
	 */
	public Dependency getDependencyByName(String dependencyName) {
		for (Dependency dependency : this.dependencies)
			if (dependency.getName().equals(dependencyName))
				return dependency;

		return null;
	}

	/**
	 * Checks if a dependency is loaded or not.
	 *
	 * @param dependencyName The dependency name used for the check
	 * @return True if the dependency has been found, false otherwise.
	 */
	public boolean isDependencyLoaded(String dependencyName) {
		return this.getDependencyByName(dependencyName) != null;
	}

	/**
	 * Load each dependency if the needed plugin is enabled.
	 */
	private void loadDependencies() {
		for (String depName : DEPENDENCIES_NAMES) {
			if (isValidPlugin(depName)) {
				try {
					Class<?> cl = Class.forName("fr.utarwyn.endercontainers.dependencies." + depName + "Dependency");
					String version = Bukkit.getPluginManager().getPlugin(depName).getDescription().getVersion();

					this.registerDependency((Dependency) cl.newInstance());
					Log.log("  Use " + depName + " (v" + version + ") as a dependency!", true);
				} catch (Exception ex) {
					System.out.println("Class of dependency \"" + depName + "\" not found! Please contact the plugin's author.");
					ex.printStackTrace();
				}
			}
		}
	}

	/**
	 * Register a dependency class into the memory
	 *
	 * @param dependency Dependency to register
	 */
	private void registerDependency(Dependency dependency) {
		dependency.onEnable();
		this.dependencies.add(dependency);
	}

	/**
	 * Know if a plugin's name is referenced to an enabled plugin on the server
	 *
	 * @param name The plugin's name to check
	 * @return True if the plugin exists and it's loaded
	 */
	private boolean isValidPlugin(String name) {
		return Bukkit.getPluginManager().isPluginEnabled(name);
	}

	/**
	 * Called when a player wants to open its enderchest by interacting with an enderchest block
	 * (This method loop loaded dependencies to call the {@link fr.utarwyn.endercontainers.dependencies.DependencyListener#onBlockChestOpened(Block, Player, boolean)} method on each of them)
	 *
	 * @param block       The block clicked by the player
	 * @param player      The player who interacts with the chest.
	 * @param sendMessage The plugin have to send a message to the player.
	 * @return True if the block chest can be opened
	 */
	@Override
	public boolean onBlockChestOpened(Block block, Player player, boolean sendMessage) {
		// Bypass foreach when no dependency is loaded.
		if (this.dependencies.isEmpty())
			return true;

		for (Dependency dependency : this.dependencies)
			if (!dependency.onBlockChestOpened(block, player, sendMessage)) {
				return false;
			}

		return true;
	}

}

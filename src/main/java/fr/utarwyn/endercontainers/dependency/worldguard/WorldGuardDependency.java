package fr.utarwyn.endercontainers.dependency.worldguard;

import fr.utarwyn.endercontainers.dependency.Dependency;
import fr.utarwyn.endercontainers.dependency.DependencyListener;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

/**
 * Dependency used to interact with the WorldGuard plugin
 * @since 2.1.0
 * @author Utarwyn
 */
public class WorldGuardDependency extends Dependency {

	private DependencyListener worldGuardHook;

	@Override
	public void onEnable() {
		String pluginVersion = this.getPluginVersion();

		if (pluginVersion != null) {
			// Instanciate the correct hook in terms of the version of WorldGuard
			if (pluginVersion.indexOf("6.") == 0) {
				this.worldGuardHook = new WorldGuardV6Hook(this.getPlugin());
			} else {
				this.worldGuardHook = new WorldGuardV7Hook(this.getPlugin());
			}
		}
	}

	@Override
	public void onDisable() {
		this.worldGuardHook = null;
	}

	@Override
	public boolean onBlockChestOpened(Block block, Player player, boolean sendMessage) {
		return this.worldGuardHook == null || this.worldGuardHook.onBlockChestOpened(block, player, sendMessage);
	}

}

package fr.utarwyn.endercontainers.dependency.worldguard;

import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.bukkit.RegionQuery;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import fr.utarwyn.endercontainers.dependency.DependencyListener;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

/**
 * Dependency used to interact with the WorldGuard V6+ plugin
 * @since 2.1.0
 * @author Utarwyn
 */
public class WorldGuardV6Hook implements DependencyListener {

	private WorldGuardPlugin worldGuardPlugin;

	WorldGuardV6Hook(Plugin plugin) {
		this.worldGuardPlugin = (WorldGuardPlugin) plugin;
	}

	@Override
	public boolean onBlockChestOpened(Block block, Player player, boolean sendMessage) {
		// OP? Ok, you can do whatever you want...
		if (player.isOp()) return true;

		// Retreive the WorldGuard Player instance and create a region query.
		LocalPlayer localPlayer = this.worldGuardPlugin.wrapPlayer(player);
		RegionQuery query = this.worldGuardPlugin.getRegionContainer().createQuery();

		// Check for denied flags at the chest's location!
		return query.testBuild(block.getLocation(), localPlayer, DefaultFlag.INTERACT, DefaultFlag.USE);
	}

}

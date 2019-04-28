package fr.utarwyn.endercontainers.dependencies;

import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.bukkit.RegionQuery;
import com.sk89q.worldguard.bukkit.WGBukkit;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class WorldGuardDependency extends Dependency {

	private WorldGuardPlugin wgPlugin;

	@Override
	public void onEnable() {
		this.wgPlugin = (WorldGuardPlugin) Bukkit.getPluginManager().getPlugin(this.getName());
	}

	@Override
	public void onDisable() {

	}

	@Override
	public boolean onBlockChestOpened(Block block, Player player, boolean sendMessage) {
		// OP? Ok, you can do whatever you want...
		if (player.isOp()) return true;

		// Retreive the WorldGuard Player instance and create a region query.
		LocalPlayer localPlayer = this.wgPlugin.wrapPlayer(player);
		RegionQuery query = WGBukkit.getPlugin().getRegionContainer().createQuery();

		// Check for denied flags at the chest's location!
		return query.testBuild(block.getLocation(), localPlayer, DefaultFlag.INTERACT, DefaultFlag.USE, DefaultFlag.CHEST_ACCESS);
	}

}

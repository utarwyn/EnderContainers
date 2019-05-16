package fr.utarwyn.endercontainers.dependency;

import fr.utarwyn.endercontainers.EnderContainers;
import fr.utarwyn.endercontainers.command.CommandManager;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.List;

public class EssentialsDependency extends Dependency {

	@Override
	public void onEnable() {
		// Remove the essentials /enderchest command from the server!
		Plugin essentialsPlugin = this.getPlugin();
		if (essentialsPlugin != null) {
			List<String> overriddenCmds = essentialsPlugin.getConfig().getStringList("overridden-commands");
			PluginCommand pluginCommand = Bukkit.getPluginCommand("essentials:enderchest");

			// Server administrators can keep up the Essentials command by adding it to the list of overridden commands.
			if (pluginCommand != null && !overriddenCmds.contains("enderchest")) {
				EnderContainers.getInstance().getManager(CommandManager.class).unregister(pluginCommand);
			}
		}
	}

	@Override
	public boolean onBlockChestOpened(Block block, Player player, boolean sendMessage) {
		return true;
	}

}

package fr.utarwyn.endercontainers.dependency;

import fr.utarwyn.endercontainers.Managers;
import fr.utarwyn.endercontainers.command.CommandManager;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.List;

/**
 * Essentials dependency.
 *
 * @author Utarwyn <maxime.malgorn@laposte.net>
 * @since 2.2.0
 */
public class EssentialsDependency implements Dependency {

    @Override
    public void onEnable(Plugin plugin) {
        // Remove the essentials /enderchest command from the server!
        List<String> overriddenCmds = plugin.getConfig().getStringList("overridden-commands");
        PluginCommand pluginCommand = Bukkit.getPluginCommand("essentials:enderchest");

        // Server administrators can keep up the Essentials command by adding it to the list of overridden commands.
        if (pluginCommand != null && !overriddenCmds.contains("enderchest")) {
            Managers.get(CommandManager.class).unregister(pluginCommand);
        }
    }

    @Override
    public void onDisable() {
        // Not implemented
    }

    @Override
    public void validateBlockChestOpening(Block block, Player player) {
        // Not implemented
    }

}

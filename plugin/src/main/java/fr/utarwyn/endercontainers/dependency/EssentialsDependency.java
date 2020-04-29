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
 * Removes /enderchest command if found.
 *
 * @author Utarwyn <maxime.malgorn@laposte.net>
 * @since 2.2.0
 */
public class EssentialsDependency extends Dependency {

    /**
     * Construct the Essentials dependency object.
     *
     * @param plugin plugin instance
     */
    public EssentialsDependency(Plugin plugin) {
        super(plugin);
    }

    @Override
    public void onEnable() {
        // Remove the essentials /enderchest command from the server!
        List<String> overriddenCmds = this.plugin.getConfig().getStringList("overridden-commands");
        PluginCommand pluginCommand = Bukkit.getPluginCommand("essentials:enderchest");

        // Server administrators can keep up the Essentials command by adding it to the list of overridden commands.
        if (pluginCommand != null && !overriddenCmds.contains("enderchest")) {
            Managers.get(CommandManager.class).unregister(pluginCommand);
        }
    }

    @Override
    public void validateBlockChestOpening(Block block, Player player) {
        // Not implemented
    }

}

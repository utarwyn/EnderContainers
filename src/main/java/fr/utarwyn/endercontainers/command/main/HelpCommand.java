package fr.utarwyn.endercontainers.command.main;

import fr.utarwyn.endercontainers.command.AbstractCommand;
import fr.utarwyn.endercontainers.util.MiscUtil;
import fr.utarwyn.endercontainers.util.PluginMsg;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

/**
 * Represents the help command of the plugin.
 *
 * @author Utarwyn
 * @since 2.0.0
 */
public class HelpCommand extends AbstractCommand {

    /**
     * Construct the command. Also define its aliases.
     */
    public HelpCommand() {
        super("help", "h", "?");
    }

    /**
     * Format a command usage for a given player and command.
     *
     * @param sender     The entity which receive the line
     * @param command    Command to perform to run the action
     * @param permission Permission which player have to had to perform the see the help line
     * @return formated command
     */
    static String formatCommandFor(CommandSender sender, String command, String permission) {
        if (!MiscUtil.senderHasPerm(sender, permission)) {
            command = ChatColor.RED.toString() + ChatColor.STRIKETHROUGH.toString() + ChatColor.stripColor(command);
        }

        return command;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void perform(CommandSender sender) {
        PluginMsg.pluginBar(sender);

        sender.sendMessage(" ");
        sender.sendMessage("  " + formatCommandFor(sender, "§6/enderchest §e[number]", "cmd"));
        sender.sendMessage("  §7   ⏩ Open your personal enderchest");
        sender.sendMessage("  " + formatCommandFor(sender, "§6/ecp open §e<player>", "openchests"));
        sender.sendMessage("  §7   ⏩ Open a player's enderchest");
        sender.sendMessage("  " + formatCommandFor(sender, "§6/ecp backup §blist,info,create,load,remove", "backups"));
        sender.sendMessage("  §7   ⏩ Manage containers backups");
        sender.sendMessage("  " + formatCommandFor(sender, "§3/ecp update", "update"));
        sender.sendMessage("  §7   ⏩ Check if the plugin is up to date");
        sender.sendMessage("  " + formatCommandFor(sender, "§3/ecp reload", "reload"));
        sender.sendMessage("  §7   ⏩ Reload the plugin");
        sender.sendMessage(" ");
    }

}

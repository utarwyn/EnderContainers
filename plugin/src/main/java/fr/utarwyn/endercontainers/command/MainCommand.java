package fr.utarwyn.endercontainers.command;

import fr.utarwyn.endercontainers.command.main.*;
import fr.utarwyn.endercontainers.util.PluginMsg;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

/**
 * Main command. Display some information about the plugin.
 *
 * @author Utarwyn
 * @since 2.0.0
 */
public class MainCommand extends AbstractCommand {

    /**
     * Stores the main author of the plugin
     */
    private final String mainAuthor;

    /**
     * Stores the current version of the plugin
     */
    private final String version;

    /**
     * Construct the main command of the plugin.
     */
    public MainCommand(Plugin plugin) {
        super("endercontainers", "ecp");

        this.addSubCommand(new HelpCommand());
        this.addSubCommand(new OpenCommand());
        this.addSubCommand(new BackupCommand());
        this.addSubCommand(new ReloadCommand());
        this.addSubCommand(new UpdateCommand());

        // Get all needed informations of the plugin in the description file
        this.mainAuthor = plugin.getDescription().getAuthors().get(0);
        this.version = plugin.getDescription().getVersion();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void perform(CommandSender sender) {
        PluginMsg.pluginBar(sender);

        sender.sendMessage(" ");
        sender.sendMessage("  §7 Plugin created by §a" + this.mainAuthor + "§7 and contributors.");
        sender.sendMessage("  §7 Version installed: §e" + this.version);
        sender.sendMessage("  §7 Do you need help? Type §d/ecp help§7!");
        sender.sendMessage(" ");
    }

}

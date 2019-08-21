package fr.utarwyn.endercontainers.command.main;

import fr.utarwyn.endercontainers.EnderContainers;
import fr.utarwyn.endercontainers.backup.BackupManager;
import fr.utarwyn.endercontainers.command.AbstractCommand;
import fr.utarwyn.endercontainers.command.backup.*;
import fr.utarwyn.endercontainers.util.PluginMsg;
import org.bukkit.command.CommandSender;

/**
 * Represents the backup management command of the plugin.
 *
 * @author Utarwyn
 * @since 2.0.0
 */
public class BackupCommand extends AbstractCommand {

    /**
     * Construct the command. Also define its subcommands.
     */
    public BackupCommand() {
        super("backup");

        BackupManager manager = EnderContainers.getInstance().getManager(BackupManager.class);

        this.addSubCommand(new ListCommand(manager));
        this.addSubCommand(new InfoCommand(manager));
        this.addSubCommand(new CreateCommand(manager));
        this.addSubCommand(new LoadCommand(manager));
        this.addSubCommand(new RemoveCommand(manager));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void perform(CommandSender sender) {
        PluginMsg.pluginBar(sender);

        sender.sendMessage(" ");
        sender.sendMessage("  " + HelpCommand.formatCommandFor(sender, "§6/ecp backup list", "backups.view"));
        sender.sendMessage("  §7   ⏩ List all created backups");
        sender.sendMessage("  " + HelpCommand.formatCommandFor(sender, "§6/ecp backup info §e[name]", "backups.info"));
        sender.sendMessage("  §7   ⏩ Get information about a backup");
        sender.sendMessage("  " + HelpCommand.formatCommandFor(sender, "§6/ecp backup create §e[name]", "backups.create"));
        sender.sendMessage("  §7   ⏩ Create a new backup");
        sender.sendMessage("  " + HelpCommand.formatCommandFor(sender, "§6/ecp backup load §e[name]", "backups.apply"));
        sender.sendMessage("  §7   ⏩ Replace all saved data by the backup");
        sender.sendMessage("  " + HelpCommand.formatCommandFor(sender, "§6/ecp backup remove §e[name]", "backups.remove"));
        sender.sendMessage("  §7   ⏩ Delete a backup with all its data");
        sender.sendMessage(" ");
    }

}

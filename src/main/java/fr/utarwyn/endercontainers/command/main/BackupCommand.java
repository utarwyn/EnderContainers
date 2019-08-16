package fr.utarwyn.endercontainers.command.main;

import fr.utarwyn.endercontainers.EnderContainers;
import fr.utarwyn.endercontainers.backup.BackupManager;
import fr.utarwyn.endercontainers.command.AbstractCommand;
import fr.utarwyn.endercontainers.command.backup.*;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class BackupCommand extends AbstractCommand {

    public BackupCommand() {
        super("backup");

        BackupManager manager = EnderContainers.getInstance().getManager(BackupManager.class);

        this.addSubCommand(new ListCommand(manager));
        this.addSubCommand(new InfoCommand(manager));
        this.addSubCommand(new CreateCommand(manager));
        this.addSubCommand(new LoadCommand(manager));
        this.addSubCommand(new RemoveCommand(manager));
    }

    @Override
    public void perform(CommandSender sender) {
        this.sendTo(sender, ChatColor.RED + "Sub-commands available: " + ChatColor.GOLD + "list,info,create,load,remove");
    }

}

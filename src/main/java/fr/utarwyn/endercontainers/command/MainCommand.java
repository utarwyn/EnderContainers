package fr.utarwyn.endercontainers.command;

import fr.utarwyn.endercontainers.EnderContainers;
import fr.utarwyn.endercontainers.command.main.*;
import org.bukkit.command.CommandSender;

public class MainCommand extends AbstractCommand {

    /**
     * Construct the main command of the plugin.
     */
    public MainCommand() {
        super("endercontainers", "ecp");

        this.addSubCommand(new HelpCommand());
        this.addSubCommand(new OpenCommand());
        this.addSubCommand(new BackupCommand());
        this.addSubCommand(new ReloadCommand());
        this.addSubCommand(new UpdateCommand());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void perform(CommandSender sender) {
        this.sendTo(sender, "Created by §3Utarwyn§7. Version §e" + EnderContainers.getInstance().getDescription().getVersion() + "§7.");
        this.sendTo(sender, "To show plugin's help: §d/ecp help§7.");
    }

}

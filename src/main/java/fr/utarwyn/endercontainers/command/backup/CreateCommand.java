package fr.utarwyn.endercontainers.command.backup;

import fr.utarwyn.endercontainers.backup.BackupManager;
import fr.utarwyn.endercontainers.command.Parameter;
import fr.utarwyn.endercontainers.configuration.Files;
import fr.utarwyn.endercontainers.util.PluginMsg;
import org.bukkit.command.CommandSender;

public class CreateCommand extends AbstractBackupCommand {

    public CreateCommand(BackupManager manager) {
        super("create", manager, "new");

        this.setPermission("backup.create");
        this.addParameter(Parameter.string());
    }

    @Override
    public void perform(CommandSender sender) {
        String name = this.readArg();

        this.sendTo(sender, Files.getLocale().getBackupCreationStarting());

        this.manager.createBackup(name, sender.getName(), result -> {
            if (result) {
                this.sendTo(sender, Files.getLocale().getBackupCreated().replace("%backup%", name));
            } else {
                PluginMsg.errorMessage(sender, Files.getLocale().getBackupExists().replace("%backup%", name));
            }
        });
    }

}

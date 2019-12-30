package fr.utarwyn.endercontainers.command.backup;

import fr.utarwyn.endercontainers.backup.BackupManager;
import fr.utarwyn.endercontainers.command.Parameter;
import fr.utarwyn.endercontainers.configuration.Files;
import fr.utarwyn.endercontainers.util.PluginMsg;
import org.bukkit.command.CommandSender;

public class RemoveCommand extends AbstractBackupCommand {

    public RemoveCommand(BackupManager manager) {
        super("remove", manager, "rm");

        this.setPermission("backup.remove");
        this.addParameter(Parameter.string());
    }

    @Override
    public void perform(CommandSender sender) {
        String name = this.readArg();

        this.manager.removeBackup(name, result -> {
            if (result) {
                this.sendTo(sender, Files.getLocale().getBackupRemoved().replace("%backup%", name));
            } else {
                PluginMsg.errorMessage(sender, Files.getLocale().getBackupUnknown().replace("%backup%", name));
            }
        });
    }

}

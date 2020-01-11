package fr.utarwyn.endercontainers.command.backup;

import fr.utarwyn.endercontainers.backup.BackupManager;
import fr.utarwyn.endercontainers.command.Parameter;
import fr.utarwyn.endercontainers.configuration.Files;
import fr.utarwyn.endercontainers.util.PluginMsg;
import org.bukkit.command.CommandSender;

public class LoadCommand extends AbstractBackupCommand {

    public LoadCommand(BackupManager manager) {
        super("load", manager, "apply");

        this.setPermission("backup.load");
        this.addParameter(Parameter.string());
    }

    @Override
    public void perform(CommandSender sender) {
        String name = this.readArg();

        this.sendTo(sender, Files.getLocale().getBackupLoadingStarted());

        this.manager.applyBackup(name, result -> {
            if (result) {
                this.sendTo(sender, Files.getLocale().getBackupLoaded().replace("%backup%", name));
            } else {
                PluginMsg.errorMessage(sender, Files.getLocale().getBackupUnknown().replace("%backup%", name));
            }
        });
    }

}

package fr.utarwyn.endercontainers.command.backup;

import fr.utarwyn.endercontainers.backup.BackupManager;
import fr.utarwyn.endercontainers.command.parameter.Parameter;
import fr.utarwyn.endercontainers.configuration.Files;
import fr.utarwyn.endercontainers.util.EUtil;
import fr.utarwyn.endercontainers.util.PluginMsg;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CreateCommand extends AbstractBackupCommand {

    public CreateCommand(BackupManager manager) {
        super("create", manager, "new");

        this.setPermission("backup.create");
        this.addParameter(Parameter.STRING);
    }

    @Override
    public void perform(CommandSender sender) {
        String name = this.readArg();

        this.sendTo(sender, Files.getLocale().getBackupCreationStarting());

        EUtil.runAsync(() -> {
            if (this.manager.createBackup(name, sender.getName())) {
                this.sendTo(sender, Files.getLocale().getBackupCreated().replace("%backup%", name));
            } else {
                PluginMsg.errorMessage(sender, Files.getLocale().getBackupExists().replace("%backup%", name));
            }
        });
    }

    @Override
    public void performPlayer(Player player) {
        // No behavior only for players for this command
    }

    @Override
    public void performConsole(CommandSender sender) {
        // No behavior only for the console for this command
    }

}

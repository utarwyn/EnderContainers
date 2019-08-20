package fr.utarwyn.endercontainers.command.backup;

import fr.utarwyn.endercontainers.EnderContainers;
import fr.utarwyn.endercontainers.backup.BackupManager;
import fr.utarwyn.endercontainers.command.Parameter;
import fr.utarwyn.endercontainers.configuration.Files;
import fr.utarwyn.endercontainers.util.MiscUtil;
import org.bukkit.ChatColor;
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

        MiscUtil.runAsync(() -> {
            if (this.manager.removeBackup(name)) {
                sender.sendMessage(EnderContainers.PREFIX + Files.getLocale().getBackupRemoved().replace("%backup%", name));
            } else {
                sender.sendMessage(EnderContainers.PREFIX + ChatColor.RED + Files.getLocale().getBackupUnknown().replace("%backup%", name));
            }
        });
    }

}

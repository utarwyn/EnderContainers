package fr.utarwyn.endercontainers.command.backup;

import fr.utarwyn.endercontainers.EnderContainers;
import fr.utarwyn.endercontainers.backup.BackupManager;
import fr.utarwyn.endercontainers.command.parameter.Parameter;
import fr.utarwyn.endercontainers.configuration.Files;
import fr.utarwyn.endercontainers.util.EUtil;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RemoveCommand extends AbstractBackupCommand {

    public RemoveCommand(BackupManager manager) {
        super("remove", manager, "rm");

        this.setPermission("backup.remove");
        this.addParameter(Parameter.STRING);
    }

    @Override
    public void perform(CommandSender sender) {
        String name = this.readArg();

        EUtil.runAsync(() -> {
            if (this.manager.removeBackup(name)) {
                sender.sendMessage(EnderContainers.PREFIX + Files.getLocale().getBackupRemoved().replace("%backup%", name));
            } else {
                sender.sendMessage(EnderContainers.PREFIX + ChatColor.RED + Files.getLocale().getBackupUnknown().replace("%backup%", name));
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

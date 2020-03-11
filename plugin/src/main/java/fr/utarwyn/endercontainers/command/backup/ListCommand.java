package fr.utarwyn.endercontainers.command.backup;

import fr.utarwyn.endercontainers.backup.Backup;
import fr.utarwyn.endercontainers.backup.BackupManager;
import fr.utarwyn.endercontainers.configuration.Files;
import fr.utarwyn.endercontainers.util.PluginMsg;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.List;

public class ListCommand extends AbstractBackupCommand {

    public ListCommand(BackupManager manager) {
        super("list", manager);

        this.setPermission("backup.list");
    }

    @Override
    public void perform(CommandSender sender) {
        List<Backup> backups = this.manager.getBackups();

        if (!backups.isEmpty()) {
            PluginMsg.pluginBar(sender);
            sender.sendMessage(" ");

            for (Backup backup : backups) {
                sender.sendMessage(
                        " §r §7 Backup §b" + backup.getName() + "§7. Created by §e" + backup.getCreatedBy() + "§7."
                );
            }

            sender.sendMessage(" ");
            sender.sendMessage(" §r §7" + Files.getLocale().getBackupInfo().replace("%command%", "/ecp backup info <name>"));
            sender.sendMessage(" §r §8♣ Backup list (page 1)");
            PluginMsg.endBar(sender);
        } else {
            this.sendTo(sender, ChatColor.RED + Files.getLocale().getBackupZero());
        }
    }

}

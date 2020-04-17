package fr.utarwyn.endercontainers.command.backup;

import fr.utarwyn.endercontainers.backup.Backup;
import fr.utarwyn.endercontainers.backup.BackupManager;
import fr.utarwyn.endercontainers.configuration.Files;
import fr.utarwyn.endercontainers.configuration.LocaleKey;
import fr.utarwyn.endercontainers.util.PluginMsg;
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
                sender.sendMessage(String.format(
                        " §r §7 Backup §b%s§7. Created by §e%s§7.",
                        backup.getName(), backup.getCreatedBy()
                ));
            }

            sender.sendMessage(" ");
            sender.sendMessage(String.format(
                    " §r §7%s",
                    Files.getLocale().getMessage(LocaleKey.CMD_BACKUP_INFO)
                            .replace("%command%", "/ecp backup info <name>")
            ));
            sender.sendMessage(" §r §8♣ Backup list (page 1)");
            PluginMsg.endBar(sender);
        } else {
            PluginMsg.errorMessageWithPrefix(sender, LocaleKey.CMD_BACKUP_ZERO);
        }
    }

}

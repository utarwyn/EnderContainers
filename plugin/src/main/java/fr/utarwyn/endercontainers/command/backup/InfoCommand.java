package fr.utarwyn.endercontainers.command.backup;

import fr.utarwyn.endercontainers.backup.Backup;
import fr.utarwyn.endercontainers.backup.BackupManager;
import fr.utarwyn.endercontainers.command.Parameter;
import fr.utarwyn.endercontainers.configuration.Files;
import fr.utarwyn.endercontainers.configuration.Locale;
import fr.utarwyn.endercontainers.configuration.LocaleKey;
import fr.utarwyn.endercontainers.util.PluginMsg;
import org.bukkit.command.CommandSender;

import java.text.DateFormat;
import java.util.Collections;
import java.util.Optional;

public class InfoCommand extends AbstractBackupCommand {

    private static final DateFormat DATE_FORMAT = DateFormat.getDateInstance(DateFormat.MEDIUM);

    public InfoCommand(BackupManager manager) {
        super("info", manager);

        this.setPermission("backup.info");
        this.addParameter(Parameter.string());
    }

    @Override
    public void perform(CommandSender sender) {
        String name = this.readArg();
        Optional<Backup> optionalBackup = this.manager.getBackupByName(name);

        if (!optionalBackup.isPresent()) {
            PluginMsg.errorMessageWithPrefix(sender, LocaleKey.CMD_BACKUP_UNKNOWN,
                    Collections.singletonMap("backup", name));
            return;
        }

        Locale locale = Files.getLocale();
        Backup backup = optionalBackup.get();

        PluginMsg.pluginBar(sender);
        sender.sendMessage(" ");
        sender.sendMessage(String.format(
                " §7   %s: §r%s",
                locale.getMessage(LocaleKey.CMD_BACKUP_LABEL_NAME),
                backup.getName()
        ));
        sender.sendMessage(String.format(
                " §7  %s: §r%s",
                locale.getMessage(LocaleKey.CMD_BACKUP_LABEL_DATE),
                DATE_FORMAT.format(backup.getDate())
        ));
        sender.sendMessage(String.format(
                "  §7  %s: §e%s",
                locale.getMessage(LocaleKey.CMD_BACKUP_LABEL_BY),
                backup.getCreatedBy()
        ));
        sender.sendMessage(" ");
        sender.sendMessage(String.format(
                " §8  %s: §d/ecp backup load %s",
                locale.getMessage(LocaleKey.CMD_BACKUP_LABEL_LOADCMD), name
        ));
        sender.sendMessage(String.format(
                " §8  %s: §c/ecp backup remove %s",
                locale.getMessage(LocaleKey.CMD_BACKUP_LABEL_RMCMD), name
        ));
        sender.sendMessage(" ");
        PluginMsg.endBar(sender);
    }

}

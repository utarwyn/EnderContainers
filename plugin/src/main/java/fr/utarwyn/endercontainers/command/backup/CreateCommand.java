package fr.utarwyn.endercontainers.command.backup;

import fr.utarwyn.endercontainers.backup.BackupManager;
import fr.utarwyn.endercontainers.command.Parameter;
import fr.utarwyn.endercontainers.configuration.LocaleKey;
import fr.utarwyn.endercontainers.util.PluginMsg;
import org.bukkit.command.CommandSender;

import java.util.Collections;

public class CreateCommand extends AbstractBackupCommand {

    public CreateCommand(BackupManager manager) {
        super("create", manager, "new");

        this.setPermission("backup.create");
        this.addParameter(Parameter.string());
    }

    @Override
    public void perform(CommandSender sender) {
        String name = this.readArg();

        PluginMsg.messageWithPrefix(sender, LocaleKey.CMD_BACKUP_CREATION_STARTED);

        this.manager.createBackup(name, sender.getName(), result -> {
            if (Boolean.TRUE.equals(result)) {
                PluginMsg.messageWithPrefix(sender, LocaleKey.CMD_BACKUP_CREATED,
                        Collections.singletonMap("backup", name));
            } else {
                PluginMsg.errorMessageWithPrefix(sender, LocaleKey.CMD_BACKUP_EXISTS,
                        Collections.singletonMap("backup", name));
            }
        });
    }

}

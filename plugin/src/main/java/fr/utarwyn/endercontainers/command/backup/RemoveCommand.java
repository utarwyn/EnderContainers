package fr.utarwyn.endercontainers.command.backup;

import fr.utarwyn.endercontainers.backup.BackupManager;
import fr.utarwyn.endercontainers.command.Parameter;
import fr.utarwyn.endercontainers.configuration.LocaleKey;
import fr.utarwyn.endercontainers.util.PluginMsg;
import org.bukkit.command.CommandSender;

import java.util.Collections;

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
            if (Boolean.TRUE.equals(result)) {
                PluginMsg.messageWithPrefix(sender, LocaleKey.CMD_BACKUP_REMOVED,
                        Collections.singletonMap("backup", name));
            } else {
                PluginMsg.errorMessageWithPrefix(sender, LocaleKey.CMD_BACKUP_UNKNOWN,
                        Collections.singletonMap("backup", name));
            }
        });
    }

}

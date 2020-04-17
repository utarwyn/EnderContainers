package fr.utarwyn.endercontainers.command.backup;

import fr.utarwyn.endercontainers.backup.BackupManager;
import fr.utarwyn.endercontainers.command.Parameter;
import fr.utarwyn.endercontainers.configuration.Files;
import fr.utarwyn.endercontainers.configuration.LocaleKey;
import fr.utarwyn.endercontainers.util.PluginMsg;
import org.bukkit.command.CommandSender;

import java.util.Collections;

public class LoadCommand extends AbstractBackupCommand {

    public LoadCommand(BackupManager manager) {
        super("load", manager, "apply");

        this.setPermission("backup.load");
        this.addParameter(Parameter.string());
    }

    @Override
    public void perform(CommandSender sender) {
        String name = this.readArg();

        PluginMsg.messageWithPrefix(sender, LocaleKey.CMD_BACKUP_LOADING_STARTED);

        this.manager.applyBackup(name, result -> {
            if (Boolean.TRUE.equals(result)) {
                PluginMsg.messageWithPrefix(sender, LocaleKey.CMD_BACKUP_LOADED,
                        Collections.singletonMap("backup", "name"));
            } else {
                PluginMsg.errorMessageWithPrefix(sender, LocaleKey.CMD_BACKUP_UNKNOWN,
                        Collections.singletonMap("backup", name));
            }
        });
    }

}

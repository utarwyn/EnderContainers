package fr.utarwyn.endercontainers.command.main;

import fr.utarwyn.endercontainers.Managers;
import fr.utarwyn.endercontainers.command.AbstractCommand;
import fr.utarwyn.endercontainers.configuration.LocaleKey;
import fr.utarwyn.endercontainers.util.PluginMsg;
import fr.utarwyn.endercontainers.util.Updater;
import org.bukkit.command.CommandSender;

public class UpdateCommand extends AbstractCommand {

    /**
     * The updater used to check for a new plugin version
     */
    private final Updater updater;

    public UpdateCommand() {
        super("update");
        this.updater = Managers.get(Updater.class);

        this.setPermission("update");
    }

    @Override
    public void perform(CommandSender sender) {
        if (!this.updater.notifyPlayer(sender)) {
            PluginMsg.messageWithPrefix(sender, LocaleKey.CMD_NO_UPDATE);
        }
    }

}

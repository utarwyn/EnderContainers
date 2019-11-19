package fr.utarwyn.endercontainers.command.main;

import fr.utarwyn.endercontainers.EnderContainers;
import fr.utarwyn.endercontainers.command.AbstractCommand;
import fr.utarwyn.endercontainers.configuration.Files;
import fr.utarwyn.endercontainers.util.Updater;
import org.bukkit.command.CommandSender;

public class UpdateCommand extends AbstractCommand {

    /**
     * The updater used to check for a new plugin version
     */
    private Updater updater;

    public UpdateCommand() {
        super("update");
        this.updater = EnderContainers.getInstance().getManager(Updater.class);

        this.setPermission("update");
    }

    @Override
    public void perform(CommandSender sender) {
        if (!this.updater.notifyPlayer(sender)) {
            sender.sendMessage(EnderContainers.PREFIX + "§7" + Files.getLocale().getNoUpdate());
        }
    }

}

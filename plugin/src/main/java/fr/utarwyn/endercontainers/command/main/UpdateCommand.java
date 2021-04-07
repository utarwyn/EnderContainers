package fr.utarwyn.endercontainers.command.main;

import fr.utarwyn.endercontainers.Managers;
import fr.utarwyn.endercontainers.command.AbstractCommand;
import fr.utarwyn.endercontainers.util.Updater;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Handles the update command (/endercontainers update).
 *
 * @author Utarwyn
 * @since 2.0.0
 */
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
    public void performPlayer(Player player) {
        this.updater.notifyPlayer(player);
    }

    @Override
    public void performConsole(CommandSender sender) {
        this.updater.notifyConsole();
    }

}

package fr.utarwyn.endercontainers.commands.main;

import fr.utarwyn.endercontainers.Config;
import fr.utarwyn.endercontainers.commands.AbstractCommand;
import fr.utarwyn.endercontainers.util.Locale;
import fr.utarwyn.endercontainers.util.Updater;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class UpdateCommand extends AbstractCommand {

	public UpdateCommand() {
		super("update");

		this.setPermission(Config.PERM_PREFIX + "update");
	}

	@Override
	public void perform(CommandSender sender) {
		if (!Updater.getInstance().isUpToDate()) {
			sender.sendMessage(Config.PREFIX + "§aThere is a newer version available: §2§l" + Updater.getInstance().getNewestVersion() + "§a.");
			sender.sendMessage(Config.PREFIX + "&7Click here to download it: " + Config.DOWNLOAD_LINK);
		} else {
			sender.sendMessage(Config.PREFIX + "§7" + Locale.noUpdate);
		}
	}

	@Override
	public void performPlayer(Player player) {

	}

	@Override
	public void performConsole(CommandSender sender) {

	}

}

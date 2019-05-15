package fr.utarwyn.endercontainers.command.main;

import fr.utarwyn.endercontainers.command.AbstractCommand;
import fr.utarwyn.endercontainers.command.parameter.Parameter;
import fr.utarwyn.endercontainers.util.EUtil;
import fr.utarwyn.endercontainers.util.PluginMsg;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class HelpCommand extends AbstractCommand {

	private static final int PAGE_NUMBER = 2;

	public HelpCommand() {
		super("help", "h", "?");

		this.addParameter(Parameter.INT.optional());
	}

	@Override
	public void perform(CommandSender sender) {
		int page = EUtil.clamp(this.readArgOrDefault(1), 1, PAGE_NUMBER);

		PluginMsg.pluginBar(sender);
		sender.sendMessage(" ");

		if (page == 1) {
			sendFormattedHelpLine(sender, "Open your enderchest", "§e/enderchest [number]", "cmd");
			sendFormattedHelpLine(sender, "Open an enderchest", "§b/ecp open <player>", "openchests");

			sendFormattedHelpLine(sender, "List backups", "§b/ecp backups", "backups.view");
			sendFormattedHelpLine(sender, "Create a backup", "§b/ecp createbackup <name>", "backups.create");
			sendFormattedHelpLine(sender, "Show backup information", "§b/ecp backup <name>", "backups.info");
		} else if (page == 2) {
			sendFormattedHelpLine(sender, "Load a backup", "§b/ecp applybackup <name>", "backups.apply");
			sendFormattedHelpLine(sender, "Remove a backup", "§b/ecp rmbackup <name>", "backups.remove");

			sender.sendMessage(" ");

			sendFormattedHelpLine(sender, "Check for updates", "§e/ecp update", "update");
			sendFormattedHelpLine(sender, "Reload the plugin", "§e/ecp reload", "reload");
		}

		sender.sendMessage(" ");
		sender.sendMessage(" §r §8✿ Plugin's help (" + page + "/" + PAGE_NUMBER + ")");
		PluginMsg.endBar(sender);
	}

	@Override
	public void performPlayer(Player player) {
		// No behavior only for players for this command
	}

	@Override
	public void performConsole(CommandSender sender) {
		// No behavior only for the console for this command
	}

	/**
	 * Send a customized line at an entity for the plugin's help
	 * @param sender The entity which receive the line
	 * @param title Title of the action item
	 * @param command Command to perform to run the action
	 * @param perm Permission which player have to had to perform the see the help line
	 */
	private void sendFormattedHelpLine(CommandSender sender, String title, String command, String perm) {
		StringBuilder message = new StringBuilder(" §7  ");
		StringBuilder hiddenCommand = new StringBuilder("§c§k");

		message.append(title).append(": ");

		for (int i = 0; i < command.length(); i++)
			hiddenCommand.append("-");

		if (EUtil.senderHasPerm(sender, perm))
			message.append(command);
		else
			message.append(hiddenCommand);

		sender.sendMessage(message.toString());
	}

}

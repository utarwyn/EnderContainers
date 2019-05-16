package fr.utarwyn.endercontainers.command.backup;

import fr.utarwyn.endercontainers.backup.Backup;
import fr.utarwyn.endercontainers.backup.BackupManager;
import fr.utarwyn.endercontainers.command.parameter.Parameter;
import fr.utarwyn.endercontainers.configuration.Files;
import fr.utarwyn.endercontainers.configuration.Locale;
import fr.utarwyn.endercontainers.util.PluginMsg;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.text.DateFormat;

public class InfoCommand extends AbstractBackupCommand {

	private static final DateFormat DATE_FORMAT = DateFormat.getDateInstance(DateFormat.MEDIUM);

	public InfoCommand(BackupManager manager) {
		super("info", manager);

		this.setPermission("backup.info");
		this.addParameter(Parameter.STRING);
	}

	@Override
	public void perform(CommandSender sender) {
		String name = this.readArg();
		Backup backup = this.manager.getBackupByName(name);
		Locale locale = Files.getLocale();

		if (backup == null) {
			this.sendTo(sender, ChatColor.RED + locale.getBackupUnknown().replace("%backup%", name));
			return;
		}

		PluginMsg.pluginBar(sender);
		sender.sendMessage(" ");
		sender.sendMessage(" " + ChatColor.GRAY + "  " + locale.getBackupLabelName() + ": §r" + backup.getName() + " §7(" + backup.getType() + ")");
		sender.sendMessage(" " + ChatColor.GRAY + "  " + locale.getBackupLabelDate() + ": §r" + DATE_FORMAT.format(backup.getDate()));
		sender.sendMessage(" " + ChatColor.GRAY + "  " + locale.getBackupLabelBy() + ": §e" + backup.getCreatedBy());
		sender.sendMessage(" ");
		sender.sendMessage(" " + ChatColor.DARK_GRAY + "  " + locale.getBackupLabelLoadCmd() + ": §d/ecp backup load " + name);
		sender.sendMessage(" " + ChatColor.DARK_GRAY + "  " + locale.getBackupLabelRmCmd() + ": §c/ecp backup remove " + name);
		sender.sendMessage(" ");
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

}

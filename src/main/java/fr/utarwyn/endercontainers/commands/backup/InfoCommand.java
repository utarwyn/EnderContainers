package fr.utarwyn.endercontainers.commands.backup;

import fr.utarwyn.endercontainers.Config;
import fr.utarwyn.endercontainers.backup.Backup;
import fr.utarwyn.endercontainers.backup.BackupManager;
import fr.utarwyn.endercontainers.commands.parameter.Parameter;
import fr.utarwyn.endercontainers.util.Locale;
import fr.utarwyn.endercontainers.util.PluginMsg;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.text.DateFormat;

public class InfoCommand extends AbstractBackupCommand {

	private static final DateFormat DATE_FORMAT = DateFormat.getDateInstance(DateFormat.MEDIUM);

	public InfoCommand(BackupManager manager) {
		super("info", manager);

		this.setPermission(Config.PERM_PREFIX + "backup.info");
		this.addParameter(Parameter.STRING);
	}

	@Override
	public void perform(CommandSender sender) {
		String name = this.readArg();
		Backup backup = this.manager.getBackupByName(name);

		if (backup == null) {
			this.sendTo(sender, ChatColor.RED + Locale.backupUnknown.replace("%backup%", name));
			return;
		}

		PluginMsg.pluginBar(sender);
		sender.sendMessage(" ");
		sender.sendMessage(" " + ChatColor.GRAY + "  " + Locale.backupLabelName + ": §r" + backup.getName() + " §7(" + backup.getType() + ")");
		sender.sendMessage(" " + ChatColor.GRAY + "  " + Locale.backupLabelDate + ": §r" + DATE_FORMAT.format(backup.getDate()));
		sender.sendMessage(" " + ChatColor.GRAY + "  " + Locale.backupLabelBy + ": §e" + backup.getCreatedBy());
		sender.sendMessage(" ");
		sender.sendMessage(" " + ChatColor.DARK_GRAY + "  " + Locale.backupLabelLoadCmd + ": §d/ecp backup load " + name);
		sender.sendMessage(" " + ChatColor.DARK_GRAY + "  " + Locale.backupLabelRmCmd + ": §c/ecp backup remove " + name);
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

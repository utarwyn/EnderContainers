package fr.utarwyn.endercontainers.commands.backup;

import fr.utarwyn.endercontainers.Config;
import fr.utarwyn.endercontainers.backup.BackupManager;
import fr.utarwyn.endercontainers.commands.parameter.Parameter;
import fr.utarwyn.endercontainers.util.EUtil;
import fr.utarwyn.endercontainers.util.Locale;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RemoveCommand extends AbstractBackupCommand {

	public RemoveCommand(BackupManager manager) {
		super("remove", manager, "rm");

		this.setPermission(Config.PERM_PREFIX + "backup.remove");
		this.addParameter(Parameter.STRING);
	}

	@Override
	public void perform(CommandSender sender) {
		String name = this.readArg();

		EUtil.runAsync(() -> {
			if (this.manager.removeBackup(name))
				sender.sendMessage(Config.PREFIX + Locale.backupRemoved.replace("%backup%", name));
			else
				sender.sendMessage(Config.PREFIX + ChatColor.RED + Locale.backupUnknown.replace("%backup%", name));
		});
	}

	@Override
	public void performPlayer(Player player) {

	}

	@Override
	public void performConsole(CommandSender sender) {

	}

}

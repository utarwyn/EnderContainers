package fr.utarwyn.endercontainers.commands.backup;

import fr.utarwyn.endercontainers.Config;
import fr.utarwyn.endercontainers.backup.BackupManager;
import fr.utarwyn.endercontainers.commands.parameter.Parameter;
import fr.utarwyn.endercontainers.util.EUtil;
import fr.utarwyn.endercontainers.util.Locale;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class LoadCommand extends AbstractBackupCommand {

	public LoadCommand(BackupManager manager) {
		super("load", manager, "apply");

		this.setPermission(Config.PERM_PREFIX + "backup.load");
		this.addParameter(Parameter.STRING);
	}

	@Override
	public void perform(CommandSender sender) {
		String name = this.readArg();

		this.sendTo(sender, Locale.backupLoadingStarted);

		EUtil.runAsync(() -> {
			if (this.manager.applyBackup(name)) {
				sender.sendMessage(Config.PREFIX + Locale.backupLoaded.replace("%backup%", name));
			} else {
				sender.sendMessage(Config.PREFIX + ChatColor.RED + Locale.backupUnknown.replace("%backup%", name));
			}
		});
	}

	@Override
	public void performPlayer(Player player) {

	}

	@Override
	public void performConsole(CommandSender sender) {

	}
}

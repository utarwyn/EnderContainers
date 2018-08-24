package fr.utarwyn.endercontainers.commands.backup;

import fr.utarwyn.endercontainers.Config;
import fr.utarwyn.endercontainers.backup.Backup;
import fr.utarwyn.endercontainers.backup.BackupManager;
import fr.utarwyn.endercontainers.util.Locale;
import fr.utarwyn.endercontainers.util.PluginMsg;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class ListCommand extends AbstractBackupCommand {

	public ListCommand(BackupManager manager) {
		super("list", manager);

		this.setPermission(Config.PERM_PREFIX + "backup.list");
	}

	@Override
	public void perform(CommandSender sender) {
		List<Backup> backups = this.manager.getBackups();

		if (backups.size() > 0) {
			PluginMsg.pluginBar(sender);
			sender.sendMessage(" ");

			for (Backup backup : backups)
				sender.sendMessage(
						" §r §7 Backup §b" + backup.getName() + "§7. Created by §e" + backup.getCreatedBy() + "§7."
				);

			sender.sendMessage(" ");
			sender.sendMessage(" §r §7" + Locale.backupInfo.replace("%command%", "/ecp backup <name>"));
			sender.sendMessage(" §r §8♣ Backup list (page 1)");
			PluginMsg.endBar(sender);
		} else {
			this.sendTo(sender, ChatColor.RED + Locale.backupZero.replace("%command%", "create"));
		}
	}

	@Override
	public void performPlayer(Player player) {

	}

	@Override
	public void performConsole(CommandSender sender) {

	}
}

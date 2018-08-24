package fr.utarwyn.endercontainers.commands.backup;

import fr.utarwyn.endercontainers.Config;
import fr.utarwyn.endercontainers.backup.BackupManager;
import fr.utarwyn.endercontainers.commands.parameter.Parameter;
import fr.utarwyn.endercontainers.util.EUtil;
import fr.utarwyn.endercontainers.util.Locale;
import fr.utarwyn.endercontainers.util.PluginMsg;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CreateCommand extends AbstractBackupCommand {

	public CreateCommand(BackupManager manager) {
		super("create", manager, "new");

		this.setPermission(Config.PERM_PREFIX + "backup.create");
		this.addParameter(Parameter.STRING);
	}

	@Override
	public void perform(CommandSender sender) {
		String name = this.readArg();

		this.sendTo(sender, Locale.backupCreationStarting);

		EUtil.runAsync(() -> {
			if (this.manager.createBackup(name, sender.getName()))
				this.sendTo(sender, Locale.backupCreated.replace("%backup%", name));
			else
				PluginMsg.errorMessage(sender, Locale.backupExists.replace("%backup%", name));
		});
	}

	@Override
	public void performPlayer(Player player) {

	}

	@Override
	public void performConsole(CommandSender sender) {

	}

}

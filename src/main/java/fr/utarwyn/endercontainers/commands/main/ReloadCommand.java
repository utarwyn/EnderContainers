package fr.utarwyn.endercontainers.commands.main;

import fr.utarwyn.endercontainers.Config;
import fr.utarwyn.endercontainers.EnderContainers;
import fr.utarwyn.endercontainers.Managers;
import fr.utarwyn.endercontainers.commands.AbstractCommand;
import fr.utarwyn.endercontainers.util.Locale;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ReloadCommand extends AbstractCommand {

	public ReloadCommand() {
		super("reload", "rl");

		this.setPermission(Config.PERM_PREFIX + "reload");
	}

	@Override
	public void perform(CommandSender sender) {
		if (!Config.get().reload()) {
			sender.sendMessage(Config.PREFIX + "§cError when reloading config! See the console for more info!");
			sender.sendMessage(Config.PREFIX + "§8Plugin now disabled.");

			Bukkit.getPluginManager().disablePlugin(EnderContainers.getInstance());
			return;
		}

		if (!Locale.get().reload()) {
			sender.sendMessage(Config.PREFIX + "§cError when reloading plugin locale! See the console for more info!");
			sender.sendMessage(Config.PREFIX + "§8Plugin now disabled.");

			Bukkit.getPluginManager().disablePlugin(EnderContainers.getInstance());
			return;
		}

		Managers.reloadAll();

		this.sendTo(sender, ChatColor.GREEN + Locale.configReloaded);
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

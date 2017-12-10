package fr.utarwyn.endercontainers.commands;

import fr.utarwyn.endercontainers.Config;
import fr.utarwyn.endercontainers.EnderContainers;
import fr.utarwyn.endercontainers.enderchest.EnderChestManager;
import fr.utarwyn.endercontainers.util.EUtil;
import fr.utarwyn.endercontainers.util.LocaleManager;
import fr.utarwyn.endercontainers.util.PluginMsg;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Class used to manage the /enderchest command
 * @since 1.0.4
 * @author Utarwyn
 */
public class EnderChestCommand implements CommandExecutor {

	/**
	 * The enderchest manager
	 */
	private EnderChestManager manager;

	/**
	 * Construct the /enderchest command
	 */
	public EnderChestCommand() {
		this.manager = EnderContainers.getInstance().getInstance(EnderChestManager.class);
	}

	/**
	 * Method called when the command /ec is performed.
	 * @param sender The entity who performed the command
	 * @param cmd The command object
	 * @param label The label used in the chat
	 * @param args Arguments used with the commande
	 * @return True if the command was performed successfully.
	 */
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player)) {
			PluginMsg.errorMessage(sender, LocaleManager.__("error_console_denied"));
			return true;
		}

		Player player = (Player) sender;

		if (!Config.enabled) {
			PluginMsg.pluginDisabled(player);
			return true;
		}

		if (Config.disabledWorlds.contains(player.getWorld().getName())) {
			PluginMsg.pluginDisabledInWorld(player);
			return true;
		}

		boolean hasParam = args.length >= 1 && StringUtils.isNumeric(args[0]);
		int index = (hasParam) ? Integer.parseInt(args[0]) - 1 : -1;

		if (hasParam && (index < 0 || index >= Config.maxEnderchests)) {
			PluginMsg.doesNotHavePerm(player);
			return true;
		}

		EUtil.runAsync(() -> {
			if (!hasParam) {
				if (player.hasPermission("cmd.enderchests"))
					this.manager.openHubMenuFor(player);
				else
					PluginMsg.doesNotHavePerm(player);

				return;
			}

			if (player.hasPermission("cmd.enderchest." + index))
				if (!this.manager.openEnderchestFor(player, index))
					player.sendMessage(EUtil.getPrefix() + ChatColor.RED + LocaleManager.__("error_cannot_open_enderchest"));
				else
					PluginMsg.doesNotHavePerm(player);
		});

		return true;
	}

}

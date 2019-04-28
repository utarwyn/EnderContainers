package fr.utarwyn.endercontainers.commands;

import fr.utarwyn.endercontainers.Config;
import fr.utarwyn.endercontainers.EnderContainers;
import fr.utarwyn.endercontainers.commands.parameter.Parameter;
import fr.utarwyn.endercontainers.enderchest.EnderChestManager;
import fr.utarwyn.endercontainers.util.EUtil;
import fr.utarwyn.endercontainers.util.Locale;
import fr.utarwyn.endercontainers.util.PluginMsg;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class EnderchestCommand extends AbstractCommand {

	private EnderChestManager manager;

	public EnderchestCommand() {
		super("enderchest", "ec", "endchest");

		this.manager = EnderContainers.getInstance().getInstance(EnderChestManager.class);

		this.addParameter(Parameter.INT.optional());
	}

	@Override
	public void perform(CommandSender sender) {
		// No global behavior for this command
	}

	@Override
	public void performPlayer(Player player) {
		if (Config.disabledWorlds.contains(player.getWorld().getName())) {
			PluginMsg.errorMessage(player, Locale.pluginWorldDisabled);
			return;
		}

		Integer argument = this.readArgOrDefault(null);
		int chestNumber = (argument != null) ? argument - 1 : -1;

		if (argument != null && (chestNumber < 0 || chestNumber >= Config.maxEnderchests)) {
			PluginMsg.accessDenied(player);
			return;
		}

		EUtil.runAsync(() -> {
			if (argument == null) {
				if (EUtil.playerHasPerm(player, "cmd.enderchests")) {
					this.manager.openHubMenuFor(player);
				} else {
					PluginMsg.accessDenied(player);
				}
			} else {
				if (EUtil.playerHasPerm(player, "cmd.enderchests") || EUtil.playerHasPerm(player, "cmd.enderchest." + chestNumber)) {
					if (!this.manager.openEnderchestFor(player, chestNumber)) {
						this.sendTo(player, ChatColor.RED + Locale.nopermOpenChest);
					}
				} else {
					PluginMsg.accessDenied(player);
				}
			}
		});
	}

	@Override
	public void performConsole(CommandSender sender) {
		PluginMsg.errorMessage(sender, Locale.nopermConsole);
	}

}

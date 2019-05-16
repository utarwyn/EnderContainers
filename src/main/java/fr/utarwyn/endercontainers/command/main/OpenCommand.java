package fr.utarwyn.endercontainers.command.main;

import fr.utarwyn.endercontainers.EnderContainers;
import fr.utarwyn.endercontainers.command.AbstractCommand;
import fr.utarwyn.endercontainers.command.parameter.Parameter;
import fr.utarwyn.endercontainers.configuration.Files;
import fr.utarwyn.endercontainers.enderchest.EnderChestManager;
import fr.utarwyn.endercontainers.util.EUtil;
import fr.utarwyn.endercontainers.util.PluginMsg;
import fr.utarwyn.endercontainers.util.uuid.UUIDFetcher;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class OpenCommand extends AbstractCommand {

	private EnderChestManager manager;

	public OpenCommand() {
		super("open");

		this.manager = EnderContainers.getInstance().getManager(EnderChestManager.class);

		this.setPermission("openchests");
		this.addParameter(Parameter.STRING.withPlayersCompletion());
	}

	@Override
	public void perform(CommandSender sender) {
		// No global behavior for this command
	}

	@Override
	public void performPlayer(Player player) {
		if (Files.getConfiguration().getDisabledWorlds().contains(player.getWorld().getName())) {
			PluginMsg.errorMessage(player, Files.getLocale().getPluginWorldDisabled());
			return;
		}

		String toInspect = this.readArg();

		EUtil.runAsync(() -> {
			Player playerToSpec = Bukkit.getPlayer(toInspect);

			if (playerToSpec == null || !playerToSpec.isOnline()) {
				UUID uuid = UUIDFetcher.getUUID(toInspect);

				if (uuid != null) {
					this.manager.openHubMenuFor(uuid, player);
				} else {
					player.sendMessage(EnderContainers.PREFIX + "§cPlayer §6" + toInspect + " §cnot found.");
				}
			} else {
				this.manager.openHubMenuFor(playerToSpec.getUniqueId(), player);
			}
		});
	}

	@Override
	public void performConsole(CommandSender sender) {
		PluginMsg.errorMessage(sender, Files.getLocale().getNopermConsole());
	}

}

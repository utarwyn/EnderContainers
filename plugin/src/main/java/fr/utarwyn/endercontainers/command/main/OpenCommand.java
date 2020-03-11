package fr.utarwyn.endercontainers.command.main;

import fr.utarwyn.endercontainers.EnderContainers;
import fr.utarwyn.endercontainers.Managers;
import fr.utarwyn.endercontainers.command.AbstractCommand;
import fr.utarwyn.endercontainers.command.Parameter;
import fr.utarwyn.endercontainers.configuration.Files;
import fr.utarwyn.endercontainers.enderchest.EnderChestManager;
import fr.utarwyn.endercontainers.util.PluginMsg;
import fr.utarwyn.endercontainers.util.uuid.UUIDFetcher;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class OpenCommand extends AbstractCommand {

    private EnderChestManager manager;

    public OpenCommand() {
        super("open");

        this.manager = Managers.get(EnderChestManager.class);

        this.setPermission("openchests");
        this.addParameter(Parameter.string().withPlayersCompletions());
    }

    @Override
    public void performPlayer(Player player) {
        if (Files.getConfiguration().getDisabledWorlds().contains(player.getWorld().getName())) {
            PluginMsg.errorMessage(player, Files.getLocale().getPluginWorldDisabled());
            return;
        }

        String playername = this.readArg();
        UUID uuid = UUIDFetcher.getUUID(playername);

        if (uuid != null) {
            this.manager.loadPlayerContext(uuid, context -> context.openHubMenuFor(player));
        } else {
            player.sendMessage(EnderContainers.PREFIX + "§cPlayer §6" + playername + " §cnot found.");
        }
    }

    @Override
    public void performConsole(CommandSender sender) {
        PluginMsg.errorMessage(sender, Files.getLocale().getNopermConsole());
    }

}

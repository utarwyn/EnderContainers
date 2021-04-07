package fr.utarwyn.endercontainers.command.main;

import fr.utarwyn.endercontainers.Managers;
import fr.utarwyn.endercontainers.command.AbstractCommand;
import fr.utarwyn.endercontainers.command.Parameter;
import fr.utarwyn.endercontainers.configuration.Files;
import fr.utarwyn.endercontainers.configuration.LocaleKey;
import fr.utarwyn.endercontainers.enderchest.EnderChestManager;
import fr.utarwyn.endercontainers.util.PluginMsg;
import fr.utarwyn.endercontainers.util.uuid.UUIDFetcher;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.UUID;

public class OpenCommand extends AbstractCommand {

    private final EnderChestManager manager;

    public OpenCommand() {
        super("open");

        this.manager = Managers.get(EnderChestManager.class);

        this.setPermission("openchests");
        this.addParameter(Parameter.string().withPlayersCompletions());
    }

    @Override
    public void performPlayer(Player player) {
        if (Files.getConfiguration().getDisabledWorlds().contains(player.getWorld().getName())) {
            PluginMsg.errorMessage(player, LocaleKey.ERR_WORLD_DISABLED);
            return;
        }

        String playername = this.readArg();
        UUID uuid = UUIDFetcher.getUUID(playername);

        if (uuid != null) {
            this.manager.loadPlayerContext(uuid, context -> context.openListInventory(player));
        } else {
            PluginMsg.errorMessage(
                    player, LocaleKey.ERR_PLAYER_NOT_FOUND,
                    Collections.singletonMap("playername", playername)
            );
        }
    }

    @Override
    public void performConsole(CommandSender sender) {
        PluginMsg.errorMessage(sender, LocaleKey.ERR_NOPERM_CONSOLE);
    }

}

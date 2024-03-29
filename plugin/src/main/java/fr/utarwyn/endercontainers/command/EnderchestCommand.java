package fr.utarwyn.endercontainers.command;

import fr.utarwyn.endercontainers.Managers;
import fr.utarwyn.endercontainers.configuration.Files;
import fr.utarwyn.endercontainers.configuration.LocaleKey;
import fr.utarwyn.endercontainers.enderchest.EnderChestManager;
import fr.utarwyn.endercontainers.util.PluginMsg;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class EnderchestCommand extends AbstractCommand {

    private static final String PERM_CHEST = "endercontainers.cmd.enderchest.%d";
    private static final String PERM_CHEST_LIST = "endercontainers.cmd.enderchests";

    private final EnderChestManager manager;

    public EnderchestCommand() {
        super("enderchest", "ec", "endchest");

        this.manager = Managers.get(EnderChestManager.class);

        this.addParameter(Parameter.integer().optional());
    }

    @Override
    public void performPlayer(Player player) {
        if (Files.getConfiguration().getDisabledWorlds().contains(player.getWorld().getName())) {
            PluginMsg.errorMessage(player, LocaleKey.ERR_WORLD_DISABLED);
            return;
        }

        Integer chestNumber = this.readArgOrDefault(null);

        if (chestNumber != null) {
            if (chestNumber > 0 && chestNumber <= Files.getConfiguration().getMaxEnderchests()) {
                this.openChestInventory(player, chestNumber - 1);
            } else {
                PluginMsg.accessDenied(player);
            }
        } else {
            this.openListInventory(player);
        }
    }

    @Override
    public void performConsole(CommandSender sender) {
        PluginMsg.errorMessage(sender, LocaleKey.ERR_NOPERM_CONSOLE);
    }

    private void openListInventory(Player player) {
        if (player.hasPermission(PERM_CHEST_LIST)) {
            this.manager.loadPlayerContext(player.getUniqueId(), context -> context.openListInventory(player));
        } else {
            PluginMsg.accessDenied(player);
        }
    }

    private void openChestInventory(Player player, int num) {
        if (player.hasPermission(PERM_CHEST_LIST) || player.hasPermission(String.format(PERM_CHEST, num))) {
            this.manager.loadPlayerContext(player.getUniqueId(), context -> {
                if (!context.openEnderchestInventory(player, num)) {
                    PluginMsg.errorMessage(player, LocaleKey.ERR_NOPERM_OPEN_CHEST);
                }
            });
        } else {
            PluginMsg.accessDenied(player);
        }
    }

}

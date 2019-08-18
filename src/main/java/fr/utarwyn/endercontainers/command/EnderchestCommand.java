package fr.utarwyn.endercontainers.command;

import fr.utarwyn.endercontainers.EnderContainers;
import fr.utarwyn.endercontainers.configuration.Files;
import fr.utarwyn.endercontainers.enderchest.EnderChestManager;
import fr.utarwyn.endercontainers.util.EUtil;
import fr.utarwyn.endercontainers.util.PluginMsg;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class EnderchestCommand extends AbstractCommand {

    private EnderChestManager manager;

    public EnderchestCommand() {
        super("enderchest", "ec", "endchest");

        this.manager = EnderContainers.getInstance().getManager(EnderChestManager.class);

        this.addParameter(Parameter.integer().optional());
    }

    @Override
    public void performPlayer(Player player) {
        if (Files.getConfiguration().getDisabledWorlds().contains(player.getWorld().getName())) {
            PluginMsg.errorMessage(player, Files.getLocale().getPluginWorldDisabled());
            return;
        }

        Integer argument = this.readArgOrDefault(null);
        int chestNumber = (argument != null) ? argument - 1 : -1;

        if (argument != null && (chestNumber < 0 || chestNumber >= Files.getConfiguration().getMaxEnderchests())) {
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
                        this.sendTo(player, ChatColor.RED + Files.getLocale().getNopermOpenChest());
                    }
                } else {
                    PluginMsg.accessDenied(player);
                }
            }
        });
    }

    @Override
    public void performConsole(CommandSender sender) {
        PluginMsg.errorMessage(sender, Files.getLocale().getNopermConsole());
    }

}

package fr.utarwyn.endercontainers.commands;

import fr.utarwyn.endercontainers.EnderContainers;
import fr.utarwyn.endercontainers.managers.EnderchestsManager;
import fr.utarwyn.endercontainers.utils.Config;
import fr.utarwyn.endercontainers.utils.CoreUtils;
import fr.utarwyn.endercontainers.utils.EnderChestUtils;
import fr.utarwyn.endercontainers.utils.PluginMsg;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class EnderChestCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) return false;
        Player p = (Player) sender;

        if (!Config.enabled) {
            CoreUtils.errorMessage(p, "Plugin is disabled.");
            return true;
        }

        if(args.length >= 1 && StringUtils.isNumeric(args[0])){
            int index = Integer.parseInt(args[0]) - 1;

            if(index < 0 || index > Config.maxEnderchests - 1){
                PluginMsg.enderchestUnknown(p, index);
                return true;
            }

            if(CoreUtils.playerHasPerm(p, "command.slot." + index) || p.isOp()){
                EnderContainers.getEnderchestsManager().openPlayerEnderChest(index, p, null);
            }else{
                PluginMsg.doesNotHavePerm(p);
            }
        }else{
            if(CoreUtils.playerHasPerm(p, "command.global") || p.isOp()){
                EnderChestUtils.openPlayerMainMenu(p, null);
            }else{
                PluginMsg.doesNotHavePerm(p);
            }
        }

        return true;
    }


}

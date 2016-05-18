package fr.utarwyn.endercontainers.commands;

import fr.utarwyn.endercontainers.EnderContainers;
import fr.utarwyn.endercontainers.utils.Config;
import fr.utarwyn.endercontainers.utils.CoreUtils;
import fr.utarwyn.endercontainers.utils.EnderChestUtils;
import fr.utarwyn.endercontainers.utils.PluginMsg;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class EnderChestCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)){
            CoreUtils.consoleDenied(sender);
            return true;
        }
        Player p = (Player) sender;

        if (!Config.enabled) {
            CoreUtils.errorMessage(p, "Plugin is disabled.");
            return true;
        }

        if(args.length >= 1 && StringUtils.isNumeric(args[0])){
            int index = Integer.parseInt(args[0]) - 1;

            if(index < -1){
                EnderContainers.getEnderchestsManager().openPlayerEnderChest(0, p, null);
                return true;
            }

            if(index < 0 || index > Config.maxEnderchests - 1){
                if(index < 0) index = 0;
                PluginMsg.enderchestUnknown(p, index);
                return true;
            }

            if(CoreUtils.playerHasPerm(p, "cmd.enderchest." + index) || p.isOp()){
                EnderContainers.getEnderchestsManager().openPlayerEnderChest(index, p, null);
            }else{
                PluginMsg.doesNotHavePerm(p);
            }
        }else{
            if(CoreUtils.playerHasPerm(p, "cmd.enderchests") || p.isOp()){
                p.playSound(p.getLocation(), Sound.valueOf(Config.openingChestSound), 1, 1);
                EnderChestUtils.openPlayerMainMenu(p, null);
            }else{
                PluginMsg.doesNotHavePerm(p);
            }
        }

        return true;
    }


}

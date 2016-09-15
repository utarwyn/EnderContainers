package fr.utarwyn.endercontainers.commands;

import fr.utarwyn.endercontainers.EnderContainers;
import fr.utarwyn.endercontainers.utils.Config;
import fr.utarwyn.endercontainers.utils.CoreUtils;
import fr.utarwyn.endercontainers.utils.PluginMsg;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
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

        final Player p = (Player) sender;

        if (!Config.enabled) {
            PluginMsg.pluginDisabled(p);
            return true;
        } else if (Config.disabledWorlds.contains(p.getWorld().getName())) {
            PluginMsg.pluginDisabledInWorld(p);
            return true;
        }

        if(args.length >= 1 && StringUtils.isNumeric(args[0])){
            final int index = Integer.parseInt(args[0]);

            if(index == 0){
                Bukkit.getScheduler().runTask(EnderContainers.getInstance(), new Runnable() {
                    @Override
                    public void run() {
                        EnderContainers.getEnderchestsManager().openPlayerEnderChest(0, p, null);
                    }
                });

                return true;
            }else if(index < 0 || index > Config.maxEnderchests - 1){
                PluginMsg.enderchestUnknown(p, (index < 0) ? 0 : index);
                return true;
            }

            if(CoreUtils.playerHasPerm(p, "cmd.enderchest." + index) || p.isOp()){
                Bukkit.getScheduler().runTask(EnderContainers.getInstance(), new Runnable() {
                    @Override
                    public void run() {
                        EnderContainers.getEnderchestsManager().openPlayerEnderChest(index, p, null);
                    }
                });
            }else{
                PluginMsg.doesNotHavePerm(p);
            }
        }else{
            if(CoreUtils.playerHasPerm(p, "cmd.enderchests") || p.isOp()){
                Bukkit.getScheduler().runTask(EnderContainers.getInstance(), new Runnable() {
                    @Override
                    public void run() {
                        playSoundTo(Config.openingChestSound, p);
                        EnderContainers.getEnderchestsManager().openPlayerMainMenu(p, null);
                    }
                });
            }else{
                PluginMsg.doesNotHavePerm(p);
            }
        }

        return true;
    }

    private void playSoundTo(String soundName, Player player){
        if(CoreUtils.soundExists(soundName))
            player.playSound(player.getLocation(), Sound.valueOf(soundName), 1F, 1F);
        else
            CoreUtils.log("§cThe sound §6" + soundName + "§c doesn't exists. Please change it in the config.", true);
    }
}

package fr.utarwyn.endercontainers.listeners;

import fr.utarwyn.endercontainers.EnderContainers;
import fr.utarwyn.endercontainers.utils.*;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;

import java.util.HashMap;

public class PluginListener implements Listener {

    private boolean disabling = false;

    @EventHandler
    public void onPluginDisable(PluginDisableEvent e) {
        if (disabling) return;
        disabling = true;

        // Clear nametags
        HashMap<Player, FloatingTextUtils.FloatingText> nametags = EnderContainers.getInstance().nameTagTask.getActiveNametags();
        for(Player p : nametags.keySet()){
            FloatingTextUtils.FloatingText ft = nametags.get(p);
            ft.remove();
        }

        try {
            CoreUtils.log(Config.pluginPrefix + "§7Save all opened enderchests...");
            EnderChestUtils.saveOpenedEnderchests();
            CoreUtils.log(Config.pluginPrefix + "§aAll enderchests are now saved in the config ! See you soon :P");
        } catch(Exception ex){
            CoreUtils.log(Config.pluginPrefix + "§cError during the save. Plugin disabled before all enderchests saved.", true);
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e){
        Player p = e.getPlayer();

        if(p.isOp() && EnderContainers.getInstance().newVersion != null){
            p.sendMessage(Config.pluginPrefix + "§a" + EnderContainers.__("other_new_update") + ": §2§l" + EnderContainers.getInstance().newVersion + "§a.");
            p.sendMessage(Config.pluginPrefix + EnderContainers.__("other_new_update_line2").replace("%command%", "/ec update"));
        }

        if(EnderContainers.hasMysql())
            EnderContainers.getMysqlManager().updatePlayerUUID(p);
        else
            EnderContainers.getEnderchestsManager().savePlayerInfo(p);
    }

}

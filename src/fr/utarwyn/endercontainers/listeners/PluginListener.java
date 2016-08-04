package fr.utarwyn.endercontainers.listeners;

import fr.utarwyn.endercontainers.EnderContainers;
import fr.utarwyn.endercontainers.utils.Config;
import fr.utarwyn.endercontainers.utils.CoreUtils;
import fr.utarwyn.endercontainers.utils.EnderChestUtils;
import fr.utarwyn.endercontainers.utils.FloatingTextUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.server.PluginDisableEvent;

import java.util.HashMap;

public class PluginListener implements Listener {

    private boolean disabling = false;

    @EventHandler
    public void onPluginDisable(PluginDisableEvent e) {
        if (disabling) return;
        disabling = true;

        try {
            // Clear nametags
            HashMap<Player, FloatingTextUtils.FloatingText> nametags = EnderContainers.getInstance().nameTagTask.getActiveNametags();
            for(Player p : nametags.keySet()){
                FloatingTextUtils.FloatingText ft = nametags.get(p);
                ft.remove();
            }

            CoreUtils.log(Config.pluginPrefix + "§7Save all opened enderchests...");
            EnderChestUtils.saveOpenedEnderchests();
            CoreUtils.log(Config.pluginPrefix + "§aAll enderchests are now saved in the config ! See you soon :P");
        } catch(Exception ex){
            CoreUtils.log(Config.pluginPrefix + "§cAn error occured during the disabling of the plugin. Please report this error to the plugin's owner.", true);
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e){
        Player p = e.getPlayer();

        if((p.isOp() || CoreUtils.playerHasPerm(p, "update")) && EnderContainers.getInstance().newVersion != null){
            p.sendMessage(Config.pluginPrefix + "§a" + EnderContainers.__("other_new_update") + ": §2§l" + EnderContainers.getInstance().newVersion + "§a.");
            p.sendMessage(Config.pluginPrefix + EnderContainers.__("other_new_update_line2").replace("%command%", "/endc update"));
        }

        EnderContainers.getEnderchestsManager().savePlayerInfo(p);
        EnderContainers.getEnderchestsManager().refreshEnderChestsOf(p);
    }

}

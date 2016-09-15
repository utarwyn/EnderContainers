package fr.utarwyn.endercontainers.listeners;

import fr.utarwyn.endercontainers.EnderContainers;
import fr.utarwyn.endercontainers.utils.Config;
import fr.utarwyn.endercontainers.utils.CoreUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PluginListener implements Listener {

    private Map<UUID, Long> lastJoins = new HashMap<>();

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e){
        final Player p = e.getPlayer();

        if((p.isOp() || CoreUtils.playerHasPerm(p, "update")) && EnderContainers.getInstance().newVersion != null){
            p.sendMessage(Config.pluginPrefix + "§a" + EnderContainers.__("other_new_update") + ": §2§l" + EnderContainers.getInstance().newVersion + "§a.");
            p.sendMessage(Config.pluginPrefix + EnderContainers.__("other_new_update_line2").replace("%command%", "/endc update"));
        }

        // Bypass
        if(lastJoins.containsKey(p.getUniqueId())){
            long lastJoin = lastJoins.get(p.getUniqueId());
            long now      = System.currentTimeMillis();

            if(now - lastJoin < Config.refreshTimeout * 1000) return;
        }

        // Save PlayerInfo & reload enderchests
        Bukkit.getScheduler().runTaskAsynchronously(EnderContainers.getInstance(), new Runnable() {
            @Override
            public void run() {
                EnderContainers.getEnderchestsManager().savePlayerInfo(p);
                EnderContainers.getEnderchestsManager().refreshEnderChestsOf(p);
            }
        });

        if(lastJoins.containsKey(p.getUniqueId())) lastJoins.remove(p.getUniqueId());
        lastJoins.put(p.getUniqueId(), System.currentTimeMillis());
    }

}

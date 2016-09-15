package fr.utarwyn.endercontainers.listeners;

import fr.utarwyn.endercontainers.EnderContainers;
import fr.utarwyn.endercontainers.utils.Config;
import fr.utarwyn.endercontainers.utils.EnderChestUtils;
import fr.utarwyn.endercontainers.utils.FloatingTextUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Set;
import java.util.UUID;

public class NameTagTask implements Runnable{

    private HashMap<UUID, FloatingTextUtils.FloatingText> playerActives = new HashMap<>();

    @Override
    public void run() {
        for(Player p : Bukkit.getOnlinePlayers()){
            Block b = p.getTargetBlock((Set<Material>) null, 8);

            // Checks
            if(b == null) continue;
            if (Config.disabledWorlds.contains(b.getWorld().getName())) continue;

            if(b.getType().equals(Material.ENDER_CHEST)){
                if(playerActives.containsKey(p.getUniqueId())) continue;

                int copEcs = EnderChestUtils.getPlayerAvailableEnderchests(p);

                String nameTag = EnderContainers.__("enderchest_nametag").replace("%enderchests%", Integer.toString(copEcs)).replaceAll("%plurial%", ((copEcs > 1) ? "s" : ""));
                FloatingTextUtils.FloatingText ft = FloatingTextUtils.displayFloatingTextAtFor(nameTag, b.getLocation().clone().add(0.5, 1, 0.5), p);

                playerActives.put(p.getUniqueId(), ft);
            }else{
                if(playerActives.containsKey(p.getUniqueId())){
                    FloatingTextUtils.removeFloatingText(playerActives.get(p.getUniqueId()));
                    playerActives.remove(p.getUniqueId());
                }
            }
        }
    }


    public HashMap<UUID, FloatingTextUtils.FloatingText> getActiveNametags(){
        return this.playerActives;
    }
}

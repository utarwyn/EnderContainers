package fr.utarwyn.endercontainers.listeners;

import fr.utarwyn.endercontainers.utils.Config;
import fr.utarwyn.endercontainers.utils.CoreUtils;
import fr.utarwyn.endercontainers.utils.EnderChestUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.Set;

import fr.utarwyn.endercontainers.utils.FloatingTextUtils;
import java.util.HashMap;

public class NameTagTask implements Runnable{

    private HashMap<Player, FloatingTextUtils.FloatingText> playerActives = new HashMap<>();

    @Override
    public void run() {
        for(Player p : Bukkit.getOnlinePlayers()){
            Block b = p.getTargetBlock((Set<Material>) null, 8);

            if(b.getType().equals(Material.ENDER_CHEST)){
                if(playerActives.containsKey(p)) continue;

                int copEcs = EnderChestUtils.getPlayerAvailableEnderchests(p);

                String nameTag = "§6§l" + copEcs + "§r§e enderchest" + ((copEcs > 1) ? "s" : "") + " available";
                FloatingTextUtils.FloatingText ft = FloatingTextUtils.displayFloatingTextAtFor(nameTag, b.getLocation().clone().add(0.5, 1, 0.5), p);

                playerActives.put(p, ft);
            }else{
                if(playerActives.containsKey(p)){
                    FloatingTextUtils.removeFloatingText(playerActives.get(p));
                    playerActives.remove(p);
                }
            }
        }
    }


    public HashMap<Player, FloatingTextUtils.FloatingText> getActiveNametags(){
        return this.playerActives;
    }
}

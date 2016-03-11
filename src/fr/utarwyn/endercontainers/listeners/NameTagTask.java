package fr.utarwyn.endercontainers.listeners;

import fr.utarwyn.endercontainers.utils.Config;
import fr.utarwyn.endercontainers.utils.CoreUtils;
import fr.utarwyn.endercontainers.utils.FloatingTextUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.*;

public class NameTagTask implements Runnable{

    private HashMap<Player, FloatingTextUtils.FloatingText> playerActives = new HashMap<Player, FloatingTextUtils.FloatingText>();

    @Override
    public void run() {
        for(Player p : Bukkit.getOnlinePlayers()){
            Block b = p.getTargetBlock((Set<Material>) null, 8);

            if(b.getType().equals(Material.ENDER_CHEST)){
                if(playerActives.containsKey(p)) continue;

                int maxEcs = Config.maxEnderchests;
                int copEcs = 0;

                for(int i = 0; i < maxEcs; i++){
                    if(CoreUtils.playerHasPerm(p, "slot." + i) || p.isOp()) copEcs++;
                }
                if(copEcs == 0) copEcs = 1;

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
}

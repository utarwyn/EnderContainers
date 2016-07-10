package fr.utarwyn.endercontainers.dependencies;

import com.massivecraft.factions.entity.BoardColl;
import com.massivecraft.factions.entity.Faction;
import com.massivecraft.factions.entity.MPlayer;
import com.massivecraft.massivecore.ps.PS;
import fr.utarwyn.endercontainers.EnderContainers;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class FactionsProtection {

    public static boolean canOpenEnderChestInFaction(Block b, Player p) {
        if (!EnderContainers.getInstance().getDependenciesManager().isDependencyLoaded("Factions")) return true;
        MPlayer mplayer = MPlayer.get(p);

        if (mplayer == null) return false;

        Faction playerFac = mplayer.getFaction();
        Faction currentFac = BoardColl.get().getFactionAt(PS.valueOf(b.getLocation()));

        if (mplayer.isOverriding()) return true;

        if(currentFac != null) {
            String facName = ChatColor.stripColor(currentFac.getName());

            if (facName.equals("Wilderness") || facName.equals("WarZone") || facName.equals("SafeZone")) return true;
            if ((playerFac != null) && (currentFac.getName().equals(playerFac.getName()))) return true;
        }

        return false;
    }

}

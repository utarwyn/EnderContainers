package fr.utarwyn.endercontainers.dependencies;

import com.massivecraft.factions.entity.BoardColl;
import com.massivecraft.factions.entity.Faction;
import com.massivecraft.factions.entity.MPlayer;
import com.massivecraft.massivecore.ps.PS;
import fr.utarwyn.endercontainers.EnderContainers;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class FactionsProtection {

    public static boolean canOpenEnderChestInFaction(Block b, Player p) {
        if (!EnderContainers.getInstance().getDependenciesManager().isDependencyLoaded("Factions")) return true;
        boolean r = false;
        MPlayer mplayer = MPlayer.get(p);

        if (mplayer == null) return r;

        Faction playerFac = mplayer.getFaction();
        Faction currentFac = BoardColl.get().getFactionAt(PS.valueOf(b.getLocation()));

        if (mplayer.isOverriding()) r = true;

        if ((!r) && (currentFac != null)) {
            if ((playerFac != null) && (currentFac.getName().equalsIgnoreCase(playerFac.getName()))) r = true;
            if (currentFac.getName().equalsIgnoreCase("§2Wilderness") ||
                    currentFac.getName().equalsIgnoreCase("§4WarZone") ||
                    currentFac.getName().equalsIgnoreCase("§6SafeZone")) r = true;
        }

        return r;
    }

}

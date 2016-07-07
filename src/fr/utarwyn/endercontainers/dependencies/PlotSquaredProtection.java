package fr.utarwyn.endercontainers.dependencies;

import com.intellectualcrafters.plot.api.PlotAPI;
import com.intellectualcrafters.plot.flag.Flags;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotBlock;
import fr.utarwyn.endercontainers.EnderContainers;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.HashSet;

public class PlotSquaredProtection {

    public static boolean canOpenEnderChestInPlot(Block b, Player p) {
        if (!EnderContainers.getInstance().getDependenciesManager().isDependencyLoaded("PlotSquared")) return true;
        PlotAPI api = new PlotAPI();
        Plot plot = api.getPlot(b.getLocation());

        if (plot == null) return true;
        if(p.isOp()) return true;

        boolean containsBlock = false;

        if(plot.hasFlag(Flags.USE)){
            for(HashSet<PlotBlock> blocks : plot.getFlag(Flags.USE).asSet()){
                for(PlotBlock plotBlock : blocks){
                    if(plotBlock.equals(PlotBlock.get(Material.ENDER_CHEST.getId(), (byte) 0))){
                        containsBlock = true;
                        break;
                    }
                }
            }
        }

        boolean flagGood = plot.hasFlag(Flags.USE) && containsBlock;
        return !(!(plot.getMembers().contains(p.getUniqueId()) || plot.getOwners().contains(p.getUniqueId())) && !flagGood);
    }
}

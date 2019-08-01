package fr.utarwyn.endercontainers.dependency;

import com.github.intellectualsites.plotsquared.plot.flag.Flags;
import com.github.intellectualsites.plotsquared.plot.object.Location;
import com.github.intellectualsites.plotsquared.plot.object.Plot;
import com.github.intellectualsites.plotsquared.plot.object.PlotBlock;
import fr.utarwyn.endercontainers.configuration.Files;
import fr.utarwyn.endercontainers.util.PluginMsg;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Optional;

/**
 * Dependency used to interact with the PlotSquared plugin
 *
 * @author Utarwyn
 * @since 1.0.6
 */
public class PlotSquaredDependency extends Dependency {

    /**
     * Material id of the enderchest block. Used by legacy servers.
     */
    private static final int ENDERCHEST_MATERIAL_ID = 130;

    /**
     * Called when a player wants to open its enderchest by interacting with an enderchest block
     *
     * @param block  The block clicked by the player
     * @param player The player who interacts with the chest
     * @return True if the block chest can be opened
     */
    @Override
    public boolean onBlockChestOpened(Block block, Player player, boolean sendMessage) {
        Location location = this.getP2Location(block.getLocation());
        if (location == null) return true;

        Plot plot = location.getPlot();
        if (plot == null || player.isOp()) return true;

        Optional<HashSet<PlotBlock>> flag = plot.getFlag(Flags.USE);

        if (flag.isPresent()) {
            boolean containsBlock = false;

            for (PlotBlock plotBlock : flag.get()) {
                if (plotBlock.equalsAny(ENDERCHEST_MATERIAL_ID, "ENDER_CHEST")) {
                    containsBlock = true;
                    break;
                }
            }

            if (containsBlock && !plot.isAdded(player.getUniqueId())) {
                if (sendMessage) {
                    PluginMsg.errorSMessage(player, Files.getLocale().getAccessDeniedPlotSq());
                }

                return false;
            }
        }

        return true;
    }

    /**
     * Transform a Bukkit Location into a PlotSquared Location.
     *
     * @param location The location to transform.
     * @return The Plot² formatted Location
     */
    private Location getP2Location(org.bukkit.Location location) {
        if (location.getWorld() != null) {
            return new Location(location.getWorld().getName(), location.getBlockX(), location.getBlockY(), location.getBlockZ());
        } else {
            return null;
        }
    }

}

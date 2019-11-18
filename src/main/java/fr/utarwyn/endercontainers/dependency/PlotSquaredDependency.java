package fr.utarwyn.endercontainers.dependency;

import com.github.intellectualsites.plotsquared.plot.flag.Flags;
import com.github.intellectualsites.plotsquared.plot.object.Location;
import com.github.intellectualsites.plotsquared.plot.object.Plot;
import com.sk89q.worldedit.world.item.ItemType;
import fr.utarwyn.endercontainers.configuration.Files;
import fr.utarwyn.endercontainers.util.PluginMsg;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.Optional;
import java.util.Set;

/**
 * Dependency used to interact with the PlotSquared plugin.
 * Works only with PlotSquared 4.390+ and Bukkit 1.13+.
 *
 * @author Utarwyn
 * @since 1.0.6
 */
public class PlotSquaredDependency extends Dependency {

    /**
     * Material name of the enderchest block.
     */
    private static final String ENDERCHEST_MATERIAL_NAME = "minecraft:enderchest_chest";

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
        Plot plot = location != null ? location.getPlot() : null;
        boolean canUse = player.isOp() || plot == null || this.canPlayerUseEnderchest(plot, player);

        if (!canUse && sendMessage) {
            PluginMsg.errorSMessage(player, Files.getLocale().getAccessDeniedPlotSq());
        }

        return canUse;
    }

    /**
     * Transform a Bukkit Location into a PlotSquared Location.
     *
     * @param location The location to transform.
     * @return The PlotSquared formatted Location
     */
    private Location getP2Location(org.bukkit.Location location) {
        if (location.getWorld() != null) {
            return new Location(location.getWorld().getName(), location.getBlockX(), location.getBlockY(), location.getBlockZ());
        } else {
            return null;
        }
    }

    /**
     * Detect if a player can use an enderchest in a specific plot.
     *
     * @param plot   place where the enderchest is
     * @param player player who wants to interact with the enderchest
     * @return true if the player can interact this plot
     */
    private boolean canPlayerUseEnderchest(Plot plot, Player player) {
        Optional<Set<ItemType>> flag = plot.getFlag(Flags.USE);
        boolean hasProtection = flag.isPresent() && flag.get().stream()
                .anyMatch(type -> type.getId().equals(ENDERCHEST_MATERIAL_NAME));

        return !hasProtection || plot.isAdded(player.getUniqueId());
    }

}

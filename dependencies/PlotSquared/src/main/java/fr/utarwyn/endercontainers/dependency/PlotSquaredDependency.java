package fr.utarwyn.endercontainers.dependency;

import com.github.intellectualsites.plotsquared.plot.flag.Flags;
import com.github.intellectualsites.plotsquared.plot.object.Location;
import com.github.intellectualsites.plotsquared.plot.object.Plot;
import com.sk89q.worldedit.world.block.BlockType;
import fr.utarwyn.endercontainers.api.dependency.dependency.Dependency;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

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
     * {@inheritDoc}
     */
    public PlotSquaredDependency(String name, Plugin plugin) {
        super(name, plugin);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onBlockChestOpened(Block block, Player player, boolean sendMessage) {
        Location location = this.getP2Location(block.getLocation());
        Plot plot = location != null ? location.getPlot() : null;
        boolean canUse = player.isOp() || plot == null || this.canPlayerUseEnderchest(plot, player);

        if (!canUse && sendMessage) {
            // TODO PluginMsg.errorSMessage(player, Files.getLocale().getAccessDeniedPlotSq());
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
        Optional<Set<BlockType>> flag = plot.getFlag(Flags.USE);
        boolean hasProtection = flag.isPresent() && flag.get().stream()
                .anyMatch(type -> type.getId().equals(ENDERCHEST_MATERIAL_NAME));

        return !hasProtection || plot.isAdded(player.getUniqueId());
    }

}

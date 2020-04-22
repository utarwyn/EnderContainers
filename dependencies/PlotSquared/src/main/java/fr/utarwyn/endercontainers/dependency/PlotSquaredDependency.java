package fr.utarwyn.endercontainers.dependency;

import com.plotsquared.core.location.Location;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.plot.flag.implementations.UseFlag;
import com.plotsquared.core.plot.flag.types.BlockTypeWrapper;
import com.sk89q.worldedit.world.block.BlockTypes;
import fr.utarwyn.endercontainers.configuration.LocaleKey;
import fr.utarwyn.endercontainers.dependency.exceptions.BlockChestOpeningException;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.List;

/**
 * Dependency used to interact with the PlotSquared plugin.
 * Works only with PlotSquared 5+ and Bukkit 1.13+.
 *
 * @author Utarwyn
 * @since 1.0.6
 */
public class PlotSquaredDependency implements Dependency {

    /**
     * {@inheritDoc}
     */
    @Override
    public void onEnable(Plugin plugin) {
        // Not implemented
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onDisable() {
        // Not implemented
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void validateBlockChestOpening(Block block, Player player)
            throws BlockChestOpeningException {
        Location location = this.getP2Location(block.getLocation());
        Plot plot = location != null ? location.getPlot() : null;

        if (!player.isOp() && plot != null && !this.canPlayerUseEnderchest(plot, player)) {
            throw new BlockChestOpeningException(LocaleKey.ERR_DEP_PLOTSQ);
        }
    }

    /**
     * Transform a Bukkit Location into a PlotSquared Location.
     *
     * @param location The location to transform.
     * @return The PlotSquared formatted Location
     */
    private Location getP2Location(org.bukkit.Location location) {
        return location.getWorld() != null ? new Location(
                location.getWorld().getName(),
                location.getBlockX(), location.getBlockY(), location.getBlockZ()
        ) : null;
    }

    /**
     * Detect if a player can use an enderchest in a specific plot.
     *
     * @param plot   place where the enderchest is
     * @param player player who wants to interact with the enderchest
     * @return true if the player can interact this plot
     */
    private boolean canPlayerUseEnderchest(Plot plot, Player player) {
        List<BlockTypeWrapper> blocks = plot.getFlag(UseFlag.class);
        boolean hasProtection = blocks.stream()
                .anyMatch(type -> type.getBlockType() == BlockTypes.ENDER_CHEST);

        return !hasProtection || plot.isAdded(player.getUniqueId());
    }

}

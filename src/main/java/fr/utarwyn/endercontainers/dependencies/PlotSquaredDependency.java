package fr.utarwyn.endercontainers.dependencies;

import com.github.intellectualsites.plotsquared.plot.flag.Flags;
import com.github.intellectualsites.plotsquared.plot.object.Location;
import com.github.intellectualsites.plotsquared.plot.object.Plot;
import com.github.intellectualsites.plotsquared.plot.object.PlotBlock;
import fr.utarwyn.endercontainers.util.Locale;
import fr.utarwyn.endercontainers.util.PluginMsg;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Optional;

/**
 * Dependency used to interact with the PlotSquared plugin
 * @since 1.0.6
 * @author Utarwyn
 */
public class PlotSquaredDependency extends Dependency {

	/**
	 * Called when a player wants to open its enderchest by interacting with an enderchest block
	 * @param block The block clicked by the player
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
			PlotBlock enderchestBlock = PlotBlock.get(Material.ENDER_CHEST.getId(), (byte) 0);

			for (PlotBlock plotBlock : flag.get()) {
				if (plotBlock.equals(enderchestBlock)) {
					containsBlock = true;
					break;
				}
			}

			if (containsBlock && !plot.isAdded(player.getUniqueId())) {
				if (sendMessage) {
					PluginMsg.errorSMessage(player, Locale.accessDeniedPlotSq);
				}

				return false;
			}
		}

		return true;
	}

	/**
	 * Transform a Bukkit Location into a PlotSquared Location.
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

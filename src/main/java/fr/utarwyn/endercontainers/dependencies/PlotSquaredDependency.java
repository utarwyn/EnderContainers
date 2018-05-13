package fr.utarwyn.endercontainers.dependencies;

import com.intellectualcrafters.plot.flag.Flags;
import com.intellectualcrafters.plot.object.Location;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotBlock;
import fr.utarwyn.endercontainers.util.Locale;
import fr.utarwyn.endercontainers.util.PluginMsg;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.HashSet;

/**
 * Dependency used to interact with the PlotSquared plugin
 * @since 1.0.6
 * @author Utarwyn
 */
public class PlotSquaredDependency extends Dependency {

	/**
	 * Construct the dependency object
	 */
	PlotSquaredDependency() {
		super("PlotSquared");
	}

	/**
	 * Called when the dependency is enabling
	 */
	@Override
	public void onEnable() {

	}

	/**
	 * Called when the dependency is disabling
	 */
	@Override
	public void onDisable() {

	}

	/**
	 * Called when a player wants to open its enderchest by interacting with an enderchest block
	 * @param block The block clicked by the player
	 * @param player The player who interacts with the chest
	 * @return True if the block chest can be opened
	 */
	@Override
	public boolean onBlockChestOpened(Block block, Player player, boolean sendMessage) {
		Plot plot = this.getP2Location(block.getLocation()).getPlot();
		if (plot == null || player.isOp()) return true;

		if (plot.hasFlag(Flags.USE)) {
			boolean containsBlock = false;

			for (HashSet<PlotBlock> blocks : plot.getFlag(Flags.USE).asSet()) {
				for (PlotBlock plotBlock : blocks) {
					if (plotBlock.equals(PlotBlock.get(Material.ENDER_CHEST.getId(), (byte) 0))) {
						containsBlock = true;
						break;
					}
				}
			}

			if (containsBlock && !plot.isAdded(player.getUniqueId())) {
				if (sendMessage)
					PluginMsg.errorSMessage(player, Locale.accessDeniedPlotSq);

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
		return new Location(location.getWorld().getName(), location.getBlockX(), location.getBlockY(), location.getBlockZ());
	}

}

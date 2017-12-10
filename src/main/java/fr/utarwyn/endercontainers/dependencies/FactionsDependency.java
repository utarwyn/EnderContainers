package fr.utarwyn.endercontainers.dependencies;

import com.massivecraft.factions.entity.BoardColl;
import com.massivecraft.factions.entity.Faction;
import com.massivecraft.factions.entity.MPlayer;
import com.massivecraft.massivecore.ps.PS;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

/**
 * Dependency used to interact with the Factions plugin
 * @since 1.0.3
 * @author Utarwyn
 */
public class FactionsDependency extends Dependency {

	/**
	 * Construct the dependency object
	 */
	FactionsDependency() {
		super("Factions");
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
	public boolean onBlockChestOpened(Block block, Player player) {
		MPlayer mplayer = MPlayer.get(player);

		if (mplayer == null) return false;
		if (mplayer.isOverriding()) return true;

		Faction playerFac = mplayer.getFaction();
		Faction currentFac = BoardColl.get().getFactionAt(PS.valueOf(block.getLocation()));

		if (currentFac != null) {
			String facName = ChatColor.stripColor(currentFac.getName());

			return facName.equals("Wilderness") || facName.equals("WarZone") || facName.equals("SafeZone") ||
					(playerFac != null) && (currentFac.getName().equals(playerFac.getName()));
		}

		return true;
	}

}

package fr.utarwyn.endercontainers.dependencies;

import com.massivecraft.factions.entity.*;
import com.massivecraft.massivecore.ps.PS;
import fr.utarwyn.endercontainers.util.Locale;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class Factions0212Dependency implements FactionsHook {

	@Override
	public boolean onBlockChestOpened(Block block, Player player) {
		MPlayer mplayer = MPlayer.get(player);

		// Bypass the check?
		if (mplayer == null) return false;
		if (mplayer.isOverriding()) return true;

		// Init checking variables
		Faction playerFac = mplayer.getFaction();
		Faction currentFac = BoardColl.get().getFactionAt(PS.valueOf(block));

		boolean canOpen = false;
		boolean playerFacIsReal = this.isRealFaction(playerFac);
		boolean currentFacIsReal = this.isRealFaction(currentFac);
		ChatColor facColor = ChatColor.WHITE;

		// Check factions custom permissions (and for all cases!)
		if (playerFacIsReal && currentFacIsReal) {
			if (currentFac == playerFac && playerFac.isPermitted(MPerm.getPermContainer(), mplayer.getRole()))
				canOpen = true;
			else if (currentFac != playerFac && currentFac.isPermitted(MPerm.getPermContainer(), currentFac.getRelationTo(playerFac)))
				canOpen = true;

			// Get relational color between factions
			facColor = playerFac.getColorTo(currentFac);
		} else
			canOpen = !currentFacIsReal;

		// Prevent to access to the enderchest if needed!
		if (!canOpen) {
			player.sendMessage(ChatColor.RED + Locale.accessDeniedFactions.replace("%faction%", facColor + currentFac.getName() + ChatColor.RED));
			return false;
		}

		return true;
	}

	private boolean isRealFaction(Faction faction) {
		return faction != null && !faction.isNone() && faction != FactionColl.get().getWarzone()
				&& faction != FactionColl.get().getSafezone();
	}

}

package fr.utarwyn.endercontainers.dependencies;

import com.massivecraft.factions.*;
import com.massivecraft.factions.zcore.fperms.Access;
import com.massivecraft.factions.zcore.fperms.PermissableAction;
import fr.utarwyn.endercontainers.util.Locale;
import fr.utarwyn.endercontainers.util.PluginMsg;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class Factions0106Dependency implements FactionsHook {

	@Override
	public boolean onBlockChestOpened(Block block, Player player, boolean sendMessage) {
		FPlayer fPlayer = FPlayers.getInstance().getByPlayer(player);

		// Bypass the check?
		if (fPlayer == null) return false;
		if (fPlayer.isAdminBypassing()) return true;

		// Init checking variables
		Faction playerFac = fPlayer.getFaction();
		Faction currentFac = Board.getInstance().getFactionAt(new FLocation(block));

		boolean canOpen;
		boolean playerFacIsReal = this.isRealFaction(playerFac);
		boolean currentFacIsReal = this.isRealFaction(currentFac);

		// Check faction custom permissions (do not support relationals perms)
		if (playerFacIsReal && currentFacIsReal)
			canOpen = (currentFac == playerFac && playerFac.getAccess(fPlayer, PermissableAction.CONTAINER) != Access.DENY);
		else {
			canOpen = !currentFacIsReal;

			// Send message only when needed (to not interfere with Factions!)!
			if (!canOpen && sendMessage)
				PluginMsg.errorSMessage(player, Locale.accessDeniedFactions.replace("%faction%", currentFac.getTag() + ChatColor.RED));
		}

		return canOpen;
	}

	private boolean isRealFaction(Faction faction) {
		return faction != null && !faction.isWilderness() && !faction.isWarZone() && !faction.isSafeZone();
	}

}

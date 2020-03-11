package fr.utarwyn.endercontainers.dependency;

import com.massivecraft.factions.*;
import com.massivecraft.factions.zcore.fperms.Access;
import com.massivecraft.factions.zcore.fperms.PermissableAction;
import fr.utarwyn.endercontainers.api.dependency.dependency.Dependency;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

/**
 * Factions dependency. Protect enderchests in enemy factions.
 * Support: Legacy Factions / FactionsUUID / SavageFactions
 *
 * @author Maxime <maxime.malgorn@laposte.net>
 * @since 2.2.0
 */
public class Factions1Dependency extends Dependency {

    public Factions1Dependency(String name, Plugin plugin) {
        super(name, plugin);
    }

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
            if (!canOpen && sendMessage) {
                /*PluginMsg.errorSMessage(player, Files.getLocale().getAccessDeniedFactions()
                        .replace("%faction%", currentFac.getTag() + ChatColor.RED));*/
            }
        }

        return canOpen;
    }

    private boolean isRealFaction(Faction faction) {
        return faction != null && !faction.isWilderness() && !faction.isWarZone() && !faction.isSafeZone();
    }

}

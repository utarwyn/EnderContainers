package fr.utarwyn.endercontainers.dependency;

import com.massivecraft.factions.*;
import com.massivecraft.factions.zcore.fperms.Access;
import com.massivecraft.factions.zcore.fperms.PermissableAction;
import fr.utarwyn.endercontainers.configuration.LocaleKey;
import fr.utarwyn.endercontainers.dependency.exceptions.BlockChestOpeningException;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.Collections;

/**
 * Factions dependency. Protect enderchests in enemy factions.
 * Support: Legacy Factions / FactionsUUID / SavageFactions
 *
 * @author Utarwyn <maxime.malgorn@laposte.net>
 * @since 2.2.0
 */
public class Factions1Dependency implements Dependency {

    @Override
    public void onEnable(Plugin plugin) {
        // Not implemented
    }

    @Override
    public void onDisable() {
        // Not implemented
    }

    @Override
    public void validateBlockChestOpening(Block block, Player player)
            throws BlockChestOpeningException {
        FPlayer fPlayer = FPlayers.getInstance().getByPlayer(player);

        // Bypass the check?
        if (fPlayer == null || fPlayer.isAdminBypassing()) return;

        // Init checking variables
        Faction playerFac = fPlayer.getFaction();
        Faction currentFac = Board.getInstance().getFactionAt(new FLocation(block));

        boolean playerFacIsReal = this.isRealFaction(playerFac);
        boolean currentFacIsReal = this.isRealFaction(currentFac);

        // Check permission between the two factions if there are real
        if (playerFacIsReal && currentFacIsReal) {
            if (currentFac != playerFac || playerFac.getAccess(fPlayer, PermissableAction.CONTAINER) == Access.DENY) {
                // Exception without message error in this case
                throw new BlockChestOpeningException();
            }
        }
        // If the current player does not have a faction
        // but trying to open a chest in a real faction
        else if (currentFacIsReal) {
            throw new BlockChestOpeningException(
                    LocaleKey.ERR_DEP_FACTIONS,
                    Collections.singletonMap("faction", currentFac.getTag())
            );
        }
    }

    private boolean isRealFaction(Faction faction) {
        return faction != null && !faction.isWilderness() && !faction.isWarZone() && !faction.isSafeZone();
    }

}

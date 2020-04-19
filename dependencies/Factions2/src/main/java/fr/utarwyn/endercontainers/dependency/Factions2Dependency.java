package fr.utarwyn.endercontainers.dependency;

import com.massivecraft.factions.entity.*;
import com.massivecraft.massivecore.ps.PS;
import fr.utarwyn.endercontainers.configuration.LocaleKey;
import fr.utarwyn.endercontainers.dependency.exceptions.BlockChestOpeningException;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.Collections;

/**
 * Factions v2 dependency. Protect enderchests in enemy factions.
 * Support: Factions with MassiveCore
 *
 * @author Utarwyn <maxime.malgorn@laposte.net>
 * @since 2.2.0
 */
public class Factions2Dependency implements Dependency {

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
        MPlayer mPlayer = MPlayer.get(player);

        // Bypass the check?
        if (mPlayer == null || mPlayer.isOverriding()) return;

        // Init checking variables
        Faction playerFac = mPlayer.getFaction();
        Faction currentFac = BoardColl.get().getFactionAt(PS.valueOf(block));
        if (currentFac == null) return;

        boolean canOpen;
        boolean playerFacIsReal = this.isRealFaction(playerFac);
        boolean currentFacIsReal = this.isRealFaction(currentFac);
        ChatColor facColor = ChatColor.WHITE;

        // Check factions custom permissions (and for all cases!)
        if (playerFacIsReal && currentFacIsReal) {
            if (currentFac == playerFac) {
                canOpen = playerFac.isPermitted(MPerm.getPermContainer(), mPlayer.getRole());
            } else {
                canOpen = currentFac.isPermitted(MPerm.getPermContainer(), currentFac.getRelationTo(playerFac));
            }

            // Get relational color between factions
            facColor = playerFac.getColorTo(currentFac);
        } else {
            canOpen = !currentFacIsReal;
        }

        if (!canOpen) {
            throw new BlockChestOpeningException(LocaleKey.ERR_DEP_FACTIONS,
                    Collections.singletonMap("faction", facColor + currentFac.getName()));
        }
    }

    /**
     * Verify if a faction is real and owned by a player.
     *
     * @param faction faction to check
     * @return true if the faction is real
     */
    private boolean isRealFaction(Faction faction) {
        return faction != null
                && !faction.isNone()
                && faction != FactionColl.get().getWarzone()
                && faction != FactionColl.get().getSafezone();
    }

}

package fr.utarwyn.endercontainers.dependency;

import com.massivecraft.factions.entity.*;
import com.massivecraft.massivecore.ps.PS;
import fr.utarwyn.endercontainers.api.dependency.dependency.Dependency;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class Factions2Dependency extends Dependency {

    public Factions2Dependency(String name, Plugin plugin) {
        super(name, plugin);
    }

    @Override
    public boolean onBlockChestOpened(Block block, Player player, boolean sendMessage) {
        MPlayer mPlayer = MPlayer.get(player);

        // Bypass the check?
        if (mPlayer == null) return false;
        if (mPlayer.isOverriding()) return true;

        // Init checking variables
        Faction playerFac = mPlayer.getFaction();
        Faction currentFac = BoardColl.get().getFactionAt(PS.valueOf(block));
        if (currentFac == null) return true;

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

        // Prevent to access to the enderchest if needed!
        // Sending the message only in a specific case!
        if (!canOpen && sendMessage) {
            /*PluginMsg.errorSMessage(player, Files.getLocale().getAccessDeniedFactions()
                    .replace("%faction%", facColor + currentFac.getName() + ChatColor.RED));*/
        }

        return canOpen;
    }

    private boolean isRealFaction(Faction faction) {
        return faction != null && !faction.isNone() && faction != FactionColl.get().getWarzone()
                && faction != FactionColl.get().getSafezone();
    }

}

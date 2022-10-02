package fr.utarwyn.endercontainers.dependency;

import com.massivecraft.factions.*;
import com.massivecraft.factions.perms.PermissibleActions;
import fr.utarwyn.endercontainers.configuration.LocaleKey;
import fr.utarwyn.endercontainers.dependency.exceptions.BlockChestOpeningException;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.Collections;

/**
 * FactionsUUID dependency. Protect enderchests in enemy factions.
 * Works with <a href="https://www.spigotmc.org/resources/factionsuuid.1035/">FactionsUUID</a>
 *
 * @author Utarwyn
 * @since 2.2.3
 */
public class FactionsUUIDDependency extends Dependency {

    /**
     * Construct the FactionsUUID dependency object.
     *
     * @param plugin plugin instance
     */
    public FactionsUUIDDependency(Plugin plugin) {
        super(plugin);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void validateBlockChestOpening(Block block, Player player)
            throws BlockChestOpeningException {
        FPlayer fPlayer = FPlayers.getInstance().getByPlayer(player);

        // Bypass the check?
        if (fPlayer == null || fPlayer.isAdminBypassing()) return;

        // Init checking variables
        Faction playerFac = fPlayer.getFaction();
        Faction currentFac = Board.getInstance().getFactionAt(new FLocation(block));

        boolean playerFacIsReal = playerFac != null && playerFac.isNormal();
        boolean currentFacIsReal = currentFac != null && currentFac.isNormal();

        // Check permission between the two factions if there are real
        if (playerFacIsReal && currentFacIsReal) {
            if (currentFac != playerFac || !playerFac.hasAccess(fPlayer, PermissibleActions.CONTAINER, new FLocation(block))) {
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

}

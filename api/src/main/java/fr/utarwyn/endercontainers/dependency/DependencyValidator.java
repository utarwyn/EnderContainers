package fr.utarwyn.endercontainers.dependency;

import fr.utarwyn.endercontainers.dependency.exceptions.BlockChestOpeningException;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

/**
 * Interface to validate specific actions through dependencies.
 *
 * @author Utarwyn <maxime.malgorn@laposte.net>
 * @since 2.0.0
 */
public interface DependencyValidator {

    /**
     * Validate the opening of an enderchest through a physical block.
     *
     * @param block  block clicked by the player
     * @param player player who interacts with the chest
     * @throws BlockChestOpeningException thrown when the player cannot open the chest
     */
    void validateBlockChestOpening(Block block, Player player)
            throws BlockChestOpeningException;

}

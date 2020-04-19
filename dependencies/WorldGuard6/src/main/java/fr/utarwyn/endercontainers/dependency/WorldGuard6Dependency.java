package fr.utarwyn.endercontainers.dependency;

import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.bukkit.RegionQuery;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import fr.utarwyn.endercontainers.dependency.exceptions.BlockChestOpeningException;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

/**
 * Dependency used to interact with the WorldGuard V6+ plugin
 *
 * @author Utarwyn
 * @since 2.1.0
 */
public class WorldGuard6Dependency implements Dependency {

    /**
     * WorldGuard plugin
     */
    private WorldGuardPlugin worldGuardPlugin;

    /**
     * {@inheritDoc}
     */
    @Override
    public void onEnable(Plugin plugin) {
        this.worldGuardPlugin = (WorldGuardPlugin) plugin;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onDisable() {
        this.worldGuardPlugin = null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void validateBlockChestOpening(Block block, Player player)
            throws BlockChestOpeningException {
        // OP? Ok, you can do whatever you want...
        if (player.isOp()) return;

        // Retrieve the WorldGuard Player instance and create a region query.
        LocalPlayer localPlayer = this.worldGuardPlugin.wrapPlayer(player);
        RegionQuery query = this.worldGuardPlugin.getRegionContainer().createQuery();

        // Check for denied flags at the chest's location!
        if (!query.testBuild(block.getLocation(), localPlayer, DefaultFlag.INTERACT, DefaultFlag.USE)) {
            throw new BlockChestOpeningException();
        }
    }
}

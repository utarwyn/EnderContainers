package fr.utarwyn.endercontainers.dependency;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import fr.utarwyn.endercontainers.dependency.exceptions.BlockChestOpeningException;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

/**
 * Dependency used to interact with the WorldGuard V7+ plugin.
 *
 * @author Utarwyn
 * @since 2.2.0
 */
public class WorldGuard7Dependency implements Dependency {

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

        // Retrieve the WorldGuard Player instance, create a region query and adapt the block location.
        LocalPlayer localPlayer = this.worldGuardPlugin.wrapPlayer(player);
        RegionQuery query = WorldGuard.getInstance().getPlatform().getRegionContainer().createQuery();
        Location location = BukkitAdapter.adapt(block.getLocation());

        // Check for denied flags at the chest's location!
        if (!query.testBuild(location, localPlayer, Flags.INTERACT, Flags.USE)) {
            throw new BlockChestOpeningException();
        }
    }

}

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
 * Interacts with the WorldGuard V7+ plugin.
 * Prevent to open chests in zones protected with the flag "chest-access".
 *
 * @author Utarwyn
 * @since 2.2.0
 */
public class WorldGuard7Dependency extends Dependency {

    /**
     * Construct the WorldGuard7 dependency object.
     *
     * @param plugin plugin instance
     */
    public WorldGuard7Dependency(Plugin plugin) {
        super(plugin);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void validateBlockChestOpening(Block block, Player player)
            throws BlockChestOpeningException {
        // OP? Ok, you can do whatever you want...
        if (player.isOp()) return;

        // Retrieve a WorldGuard player object, create a region query and adapt the block location.
        WorldGuardPlugin wgPlugin = (WorldGuardPlugin) this.plugin;
        LocalPlayer localPlayer = wgPlugin.wrapPlayer(player);
        RegionQuery query = WorldGuard.getInstance().getPlatform().getRegionContainer().createQuery();
        Location location = BukkitAdapter.adapt(block.getLocation());

        // Check for denied flags at the chest's location!
        if (!query.testBuild(location, localPlayer, Flags.CHEST_ACCESS)) {
            throw new BlockChestOpeningException();
        }
    }

}

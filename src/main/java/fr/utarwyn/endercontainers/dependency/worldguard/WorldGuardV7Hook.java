package fr.utarwyn.endercontainers.dependency.worldguard;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import fr.utarwyn.endercontainers.dependency.DependencyListener;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

/**
 * Dependency used to interact with the WorldGuard V7+ plugin
 *
 * @author Utarwyn
 * @since 2.2.0
 */
public class WorldGuardV7Hook implements DependencyListener {

    private WorldGuardPlugin worldGuardPlugin;

    WorldGuardV7Hook(Plugin plugin) {
        this.worldGuardPlugin = (WorldGuardPlugin) plugin;
    }

    @Override
    public boolean onBlockChestOpened(Block block, Player player, boolean sendMessage) {
        // OP? Ok, you can do whatever you want...
        if (player.isOp()) return true;

        // Retrieve the WorldGuard Player instance, create a region query and adapt the block location.
        LocalPlayer localPlayer = this.worldGuardPlugin.wrapPlayer(player);
        RegionQuery query = WorldGuard.getInstance().getPlatform().getRegionContainer().createQuery();
        Location location = BukkitAdapter.adapt(block.getLocation());

        // Check for denied flags at the chest's location!
        return query.testBuild(location, localPlayer, Flags.INTERACT, Flags.USE);
    }

}

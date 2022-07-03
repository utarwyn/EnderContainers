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
 * Interacts with the WorldGuard V6+ plugin.
 * Prevent to open chests in zones protected with the flag "chest-access".
 *
 * @author Utarwyn
 * @since 2.1.0
 */
public class WorldGuard6Dependency extends Dependency {

    /**
     * Construct the WorldGuard6 dependency object.
     *
     * @param plugin plugin instance
     */
    public WorldGuard6Dependency(Plugin plugin) {
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

        // Retrieve the WorldGuard Player instance and create a region query.
        WorldGuardPlugin wgPlugin = (WorldGuardPlugin) this.plugin;
        LocalPlayer localPlayer = wgPlugin.wrapPlayer(player);
        RegionQuery query = wgPlugin.getRegionContainer().createQuery();

        // Check for denied flags at the chest's location!
        if (!query.testBuild(block.getLocation(), localPlayer, DefaultFlag.CHEST_ACCESS)) {
            throw new BlockChestOpeningException();
        }
    }
}

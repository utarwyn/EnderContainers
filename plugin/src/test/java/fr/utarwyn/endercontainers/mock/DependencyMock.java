package fr.utarwyn.endercontainers.mock;

import fr.utarwyn.endercontainers.dependency.Dependency;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

/**
 * Fake object to test classes about dependencies.
 *
 * @author Utarwyn <maximemalgorn@gmail.com>
 */
public class DependencyMock extends Dependency {

    /**
     * Construct a fake dependency object.
     *
     * @param plugin plugin instance
     */
    public DependencyMock(Plugin plugin) {
        super(plugin);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void validateBlockChestOpening(Block block, Player player) {
        throw new UnsupportedOperationException();
    }

}

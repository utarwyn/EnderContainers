package fr.utarwyn.endercontainers.dependency;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

/**
 * Fake object to test classes about dependencies.
 *
 * @author Utarwyn <maximemalgorn@gmail.com>
 */
public class FakeDependency extends Dependency {

    /**
     * Construct a fake dependency object.
     *
     * @param plugin plugin instance
     */
    public FakeDependency(Plugin plugin) {
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

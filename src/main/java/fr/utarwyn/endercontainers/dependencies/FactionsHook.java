package fr.utarwyn.endercontainers.dependencies;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public interface FactionsHook {

	boolean onBlockChestOpened(Block block, Player player);

}

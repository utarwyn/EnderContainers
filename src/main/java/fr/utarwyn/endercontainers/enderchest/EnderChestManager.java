package fr.utarwyn.endercontainers.enderchest;

import fr.utarwyn.endercontainers.AbstractManager;
import fr.utarwyn.endercontainers.configuration.Files;
import fr.utarwyn.endercontainers.menu.EnderChestHubMenu;
import fr.utarwyn.endercontainers.menu.Menus;
import fr.utarwyn.endercontainers.storage.StorageWrapper;
import fr.utarwyn.endercontainers.storage.player.PlayerData;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

/**
 * The new enderchest manager to manage all chests
 * @since 2.0.0
 * @author Utarwyn
 */
public class EnderChestManager extends AbstractManager {

	/**
	 * A list which contains all the stored enderchests
	 */
	private List<EnderChest> enderchests;

	/**
	 * The purge task which removed the cache periodically
	 */
	private EnderChestPurgeTask purgeTask;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void initialize() {
		this.registerListener(new EnderChestListener(this));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void load() {
		this.enderchests = new ArrayList<>();
		// Start the purge task
		this.purgeTask = new EnderChestPurgeTask(this);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void unload() {
		// Close all menus
		Menus.closeAll();

		// Last purge & stop the purge task
		this.purgeTask.run();
		this.purgeTask.cancel();

		// Unload all data
		StorageWrapper.unload(PlayerData.class);
	}

	/**
	 * Returns a specific chest by its owner and its number
	 * @param owner The owner of the chest
	 * @param num The number of the chest
	 * @return The found chest, null otherwise.
	 */
	public EnderChest getEnderChest(UUID owner, int num) {
		for (EnderChest chest : this.enderchests)
			if (chest.getNum() == num && chest.getOwner() == owner)
				return chest;

		EnderChest chest = new EnderChest(owner, num);
		this.enderchests.add(chest);
		return chest;
	}

	/**
	 * Count the number of enderchests of a specific owner
	 * @param owner The UUID of the owner
	 * @return The number of enderchests owned by the UUID
	 */
	public int getEnderchestsNbOf(UUID owner) {
		int nb = 0;

		for (int i = 0; i < Files.getConfiguration().getMaxEnderchests(); i++) {
			EnderChest ec = this.getEnderChest(owner, i);
			// Reload chest information before display the hologram.
			ec.reloadMeta();

			if (ec.isAccessible())
				nb++;
		}

		return nb;
	}

	/**
	 * Permits to open the Hub menu with the list of enderchests
	 * of a specific player to an entity.
	 *
	 * @param owner The owner of enderchests to open
	 * @param viewer The player whom to send the menu
	 */
	public void openHubMenuFor(UUID owner, Player viewer) {
		EnderChestHubMenu hubMenu = new EnderChestHubMenu(owner);

		hubMenu.prepare();
		hubMenu.open(viewer);
	}

	/**
	 * Permits to open the Hub menu with the list of enderchests
	 * of a specific player to him.
	 *
	 * @param player The owner of enderchests to open to him
	 */
	public void openHubMenuFor(Player player) {
		this.openHubMenuFor(player.getUniqueId(), player);
	}

	/**
	 * Permits to open an enderchest to its owner.
	 *
	 * @param player The owner of enderchests to open to him
	 * @param num The number of the enderchest to open
	 */
	public boolean openEnderchestFor(Player player, int num) {
		EnderChest chest = this.getEnderChest(player.getUniqueId(), num);

		// Reload chest's metas before trying to open it.
		chest.reloadMeta();

		if (!chest.isAccessible()) return false;

		chest.openContainerFor(player);
		return true;
	}

	/**
	 * Method called by the {@link EnderChestPurgeTask} to delete unused chest objects in memory
	 */
	void deleteUnusedChests() {
		Iterator<EnderChest> chestIterator = this.enderchests.iterator();

		while (chestIterator.hasNext()) {
			EnderChest chest = chestIterator.next();

			if (chest.isUnused()) {
				chest.destroyContainer();
				chestIterator.remove();
			}
		}
	}

	/**
	 * Purge all chests of a player from memory
	 */
	void deleteChestsOf(Player player) {
		Iterator<EnderChest> chestIterator = this.enderchests.iterator();

		while (chestIterator.hasNext()) {
			EnderChest chest = chestIterator.next();

			if (chest.getOwner().equals(player.getUniqueId())) {
				chest.destroyContainer();
				chestIterator.remove();
			}
		}
	}

}

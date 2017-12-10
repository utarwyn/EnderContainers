package fr.utarwyn.endercontainers.enderchest;

import fr.utarwyn.endercontainers.EnderContainers;
import fr.utarwyn.endercontainers.storage.StorageWrapper;
import fr.utarwyn.endercontainers.storage.player.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

/**
 * Class which have to clear unused java objects from cache
 * to do not use too much memory space.
 *
 * @since 2.0.0
 * @author Utarwyn
 */
public class EnderChestPurgeTask implements Runnable {

	/**
	 * The enderchest manager
	 */
	private EnderChestManager manager;

	/**
	 * The BukkitTask object which manage the purge task
	 */
	private BukkitTask task;

	/**
	 * Construct the purge task
	 * @param manager The enderchest manager
	 */
	EnderChestPurgeTask(EnderChestManager manager) {
		this.manager = manager;

		// Run this task every 5 minutes
		this.task = Bukkit.getScheduler().runTaskTimerAsynchronously(EnderContainers.getInstance(), this, 5 * 60 * 20L, 5 * 60 * 20L);
	}

	/**
	 * Called automatically by the {@link org.bukkit.scheduler.BukkitScheduler}
	 */
	@Override
	public void run() {
		this.manager.deleteUnusedChests();
		StorageWrapper.clearUnusedCache(PlayerData.class);
	}

	/**
	 * Method called to cancel the purge task
	 */
	void cancel() {
		this.task.cancel();
	}

}

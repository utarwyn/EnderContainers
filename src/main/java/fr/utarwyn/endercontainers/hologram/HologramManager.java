package fr.utarwyn.endercontainers.hologram;

import fr.utarwyn.endercontainers.AbstractManager;
import fr.utarwyn.endercontainers.Config;
import fr.utarwyn.endercontainers.EnderContainers;
import fr.utarwyn.endercontainers.dependencies.DependenciesManager;
import fr.utarwyn.endercontainers.enderchest.EnderChestManager;
import fr.utarwyn.endercontainers.util.Locale;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * The hologram manager. It runs automatically a task to show/hide
 * holograms to player who needs to.
 *
 * @since 2.0.0
 * @author Utarwyn
 */
public class HologramManager extends AbstractManager implements Runnable {

	/**
	 * The enderchest manager
	 */
	private EnderChestManager chestManager;

	/**
	 * The dependencies manager
	 */
	private DependenciesManager dependenciesManager;

	/**
	 * The BukkitTask object which manage the spawning/dispawning of holograms
	 */
	private BukkitTask task;

	/**
	 * All stored holograms by owner
	 */
	private Map<UUID, Hologram> holograms;

	/**
	 * Construct the hologram manager
	 */
	public HologramManager() {
		super(EnderContainers.getInstance());
	}

	/**
	 * Called when the manager is initializing
	 */
	@Override
	public void load() {
		this.chestManager = EnderContainers.getInstance().getInstance(EnderChestManager.class);
		this.dependenciesManager = EnderContainers.getInstance().getInstance(DependenciesManager.class);

		this.task = Bukkit.getScheduler().runTaskTimer(EnderContainers.getInstance(), this, 20L, 5L);
		this.holograms = new HashMap<>();
	}

	/**
	 * Called when the manager is unloading
	 */
	@Override
	protected void unload() {
		if (this.task != null) {
			this.task.cancel();
			this.task = null;
		}

		for (Hologram hologram : this.holograms.values())
			hologram.destroy();
	}

	/**
	 * Called automatically by the BukkitTask
	 */
	@Override
	public void run() {
		if (!Config.blockNametag) return;

		for (Player player : Bukkit.getOnlinePlayers()) {
			if (Config.disabledWorlds.contains(player.getWorld().getName())) continue;
			UUID uuid = player.getUniqueId();

			Block b = player.getTargetBlock((Set<Material>) null, 6);
			if (b == null) continue;

			if (b.getType().equals(Material.ENDER_CHEST)) {
				if (this.holograms.containsKey(uuid)) continue;

				// Check player perms before displaying the hologram
				if (!this.dependenciesManager.onBlockChestOpened(b, player, false))
					continue;

				int copEcs = this.chestManager.getEnderchestsNbOf(uuid);

				String title = Locale.chestNametag
						.replace("%enderchests%", String.valueOf(copEcs))
						.replace("%maxenderchests%", String.valueOf(Config.maxEnderchests))
						.replaceAll("%plurial%", ((copEcs > 1) ? "s" : ""));

				this.holograms.put(player.getUniqueId(), new Hologram(player, title, b.getLocation()));
			} else {
				if (this.holograms.containsKey(uuid)) {
					this.holograms.get(uuid).destroy();
					this.holograms.remove(uuid);
				}
			}
		}

		// Clear unused holograms
		this.holograms.entrySet().removeIf(entry -> !entry.getValue().isPlayerOnline());
	}

}

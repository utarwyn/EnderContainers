package fr.utarwyn.endercontainers.hologram;

import fr.utarwyn.endercontainers.AbstractManager;
import fr.utarwyn.endercontainers.Managers;
import fr.utarwyn.endercontainers.configuration.Files;
import fr.utarwyn.endercontainers.dependency.DependenciesManager;
import fr.utarwyn.endercontainers.enderchest.EnderChestManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The hologram manager. It runs automatically a task to show/hide
 * holograms to player who needs to.
 *
 * @author Utarwyn
 * @since 2.0.0
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
     * All stored holograms by owner
     */
    private ConcurrentHashMap<UUID, Hologram> holograms;

    /**
     * The BukkitTask object which manage the spawning/dispawning of holograms
     */
    private BukkitTask task;

    /**
     * Generate a title with custom data for a block nametag.
     *
     * @param chestCount accessible enderchest count
     * @return the generated title with all replaced data
     */
    private static String generateNametagTitle(int chestCount) {
        int max = Files.getConfiguration().getMaxEnderchests();

        return Files.getLocale().getChestNametag()
                .replace("%enderchests%", String.valueOf(chestCount))
                .replace("%maxenderchests%", String.valueOf(max))
                .replace("%plural%", ((chestCount > 1) ? "s" : ""));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void load() {
        this.chestManager = Managers.get(EnderChestManager.class);
        this.dependenciesManager = Managers.get(DependenciesManager.class);
        this.holograms = new ConcurrentHashMap<>();

        // Start the task only if the block nametag is enabled
        if (Files.getConfiguration().isBlockNametag()) {
            this.task = this.plugin.getServer().getScheduler().runTaskTimer(this.plugin, this, 20L, 5L);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void unload() {
        if (this.task != null) {
            this.task.cancel();
            this.task = null;
        }

        this.holograms.values().forEach(Hologram::destroy);
        this.holograms.clear();
    }

    /**
     * Called automatically by the BukkitTask
     */
    @Override
    public void run() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (Files.getConfiguration().getDisabledWorlds().contains(player.getWorld().getName())) continue;

            UUID uuid = player.getUniqueId();
            Block block = player.getTargetBlock(null, 6);

            if (Material.ENDER_CHEST.equals(block.getType())) {
                if (!this.holograms.containsKey(uuid) && this.dependenciesManager.onBlockChestOpened(block, player, false)) {
                    this.spawnHologramFor(player, block);
                }
            } else if (this.holograms.containsKey(uuid)) {
                this.holograms.get(uuid).destroy();
                this.holograms.remove(uuid);
            }
        }

        // Clear unused holograms
        this.holograms.entrySet().removeIf(entry -> !entry.getValue().isPlayerOnline());
    }

    /**
     * Spawn an hologram for a specific player and a specific enderchest.
     *
     * @param observer observer of the hologram
     * @param block    where the hologram has to spawn
     */
    private void spawnHologramFor(Player observer, Block block) {
        int count = this.chestManager.getEnderchestsNbOf(observer.getUniqueId());
        String title = HologramManager.generateNametagTitle(count);
        Location location = block.getLocation().clone().add(.5, Hologram.LINE_HEIGHT - 1.25D, .5);

        this.holograms.put(observer.getUniqueId(), new Hologram(observer, title, location));
    }

}

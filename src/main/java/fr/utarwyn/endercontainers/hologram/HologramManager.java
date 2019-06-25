package fr.utarwyn.endercontainers.hologram;

import fr.utarwyn.endercontainers.AbstractManager;
import fr.utarwyn.endercontainers.configuration.Files;
import fr.utarwyn.endercontainers.dependency.DependenciesManager;
import fr.utarwyn.endercontainers.enderchest.EnderChestManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

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
     * The BukkitTask object which manage the spawning/dispawning of holograms
     */
    private BukkitTask task;

    /**
     * All stored holograms by owner
     */
    private Map<UUID, Hologram> holograms;

    /**
     * {@inheritDoc}
     */
    @Override
    public void load() {
        this.chestManager = this.plugin.getManager(EnderChestManager.class);
        this.dependenciesManager = this.plugin.getManager(DependenciesManager.class);

        this.task = this.plugin.getServer().getScheduler().runTaskTimer(this.plugin, this, 20L, 5L);
        this.holograms = new HashMap<>();
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

        for (Hologram hologram : this.holograms.values()) {
            hologram.destroy();
        }
    }

    /**
     * Called automatically by the BukkitTask
     */
    @Override
    public void run() {
        if (!Files.getConfiguration().isBlockNametag()) return;

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (Files.getConfiguration().getDisabledWorlds().contains(player.getWorld().getName())) continue;

            UUID uuid = player.getUniqueId();
            Block b = player.getTargetBlock(null, 6);

            if (Material.ENDER_CHEST.equals(b.getType())) {
                if (this.holograms.containsKey(uuid)) continue;

                // Check player perms before displaying the hologram
                if (!this.dependenciesManager.onBlockChestOpened(b, player, false))
                    continue;

                int copEcs = this.chestManager.getEnderchestsNbOf(uuid);

                String title = Files.getLocale().getChestNametag()
                        .replace("%enderchests%", String.valueOf(copEcs))
                        .replace("%maxenderchests%", String.valueOf(Files.getConfiguration().getMaxEnderchests()))
                        .replaceAll("%plurial%", ((copEcs > 1) ? "s" : ""));

                this.holograms.put(player.getUniqueId(), new Hologram(player, title, b.getLocation()));
            } else if (this.holograms.containsKey(uuid)) {
                this.holograms.get(uuid).destroy();
                this.holograms.remove(uuid);
            }
        }

        // Clear unused holograms
        this.holograms.entrySet().removeIf(entry -> !entry.getValue().isPlayerOnline());
    }

}

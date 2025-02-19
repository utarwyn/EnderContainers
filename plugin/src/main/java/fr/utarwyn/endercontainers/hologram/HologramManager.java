package fr.utarwyn.endercontainers.hologram;

import fr.utarwyn.endercontainers.AbstractManager;
import fr.utarwyn.endercontainers.Managers;
import fr.utarwyn.endercontainers.compatibility.ArmorStandAdapter;
import fr.utarwyn.endercontainers.compatibility.CompatibilityHelper;
import fr.utarwyn.endercontainers.configuration.Files;
import fr.utarwyn.endercontainers.configuration.LocaleKey;
import fr.utarwyn.endercontainers.dependency.DependenciesManager;
import fr.utarwyn.endercontainers.dependency.exceptions.BlockChestOpeningException;
import fr.utarwyn.endercontainers.enderchest.EnderChestManager;
import fr.utarwyn.endercontainers.enderchest.context.PlayerContext;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Predicate;
import java.util.logging.Level;

/**
 * The hologram manager. It runs automatically a task to show/hide
 * holograms to player who needs to.
 *
 * @author Utarwyn
 * @since 2.0.0
 */
public class HologramManager extends AbstractManager implements Runnable {

    /**
     * All stored holograms by owner
     */
    ConcurrentMap<UUID, Hologram> holograms;
    /**
     * The BukkitTask object which manage the spawning/dispawning of holograms
     */
    BukkitTask task;
    /**
     * The armor stand adapter
     */
    ArmorStandAdapter armorStandAdapter;
    /**
     * The enderchest manager
     */
    private EnderChestManager chestManager;
    /**
     * The dependencies manager
     */
    private DependenciesManager dependenciesManager;

    /**
     * Generate a title with custom data for a block nametag.
     *
     * @param chestCount accessible enderchest count
     * @return the generated title with all replaced data
     */
    private static String generateNametagTitle(int chestCount) {
        int max = Files.getConfiguration().getMaxEnderchests();

        return Files.getLocale().getMessage(LocaleKey.MISC_CHEST_NAMETAG)
                .replace("%enderchests%", String.valueOf(chestCount))
                .replace("%maxenderchests%", String.valueOf(max))
                .replace("%plural%", ((chestCount > 1) ? "s" : ""));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void load() {
        this.chestManager = Managers.get(EnderChestManager.class);
        this.dependenciesManager = Managers.get(DependenciesManager.class);
        this.armorStandAdapter = CompatibilityHelper.createArmorStandAdapter();
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
    protected synchronized void unload() {
        if (this.task != null) {
            this.task.cancel();
            this.task = null;
        }

        this.holograms.values().forEach(this::destroyHologram);
        this.holograms.clear();
    }

    /**
     * Task runner
     */
    @Override
    public void run() {
        List<String> disabledWorlds = Files.getConfiguration().getDisabledWorlds();

        // We have to check hologram status for all players in enabled worlds
        Bukkit.getOnlinePlayers().stream()
                .filter(player -> !disabledWorlds.contains(player.getWorld().getName()))
                .forEach(this::checkHologramStatus);

        // Unused holograms can be destroyed
        Predicate<Map.Entry<UUID, Hologram>> removePredicate = entry -> !entry.getValue().isObserverOnline();

        this.holograms.entrySet()
                .stream()
                .filter(removePredicate)
                .forEach(entry -> this.destroyHologram(entry.getValue()));

        this.holograms.entrySet().removeIf(removePredicate);
    }

    /**
     * Check the status of the hologram for a player.
     *
     * @param player player to handle
     */
    private void checkHologramStatus(Player player) {
        UUID uuid = player.getUniqueId();
        Block block = player.getTargetBlock(null, 6);

        if (Material.ENDER_CHEST.equals(block.getType())) {
            if (!this.holograms.containsKey(uuid)) {
                try {
                    this.dependenciesManager.validateBlockChestOpening(block, player);
                } catch (BlockChestOpeningException ignored) {
                    return;
                }

                this.chestManager.loadPlayerContext(player.getUniqueId(),
                        context -> this.spawnHologram(context, player, block));
            }
        } else if (this.holograms.containsKey(uuid)) {
            this.destroyHologram(this.holograms.remove(uuid));
        }
    }

    /**
     * Spawn an hologram above a block for a specific player.
     *
     * @param context  Context in which all chests are loaded
     * @param observer Player for which the hologram should spawn
     * @param block    Enderchest for which the hologram have to appear
     */
    private void spawnHologram(PlayerContext context, Player observer, Block block) {
        int count = context.getAccessibleChestCount();
        String title = HologramManager.generateNametagTitle(count);
        Location location = block.getLocation().clone().add(.5, -0.79D - Hologram.LINE_HEIGHT, .5);

        try {
            int entityId = this.armorStandAdapter.spawnArmorStandFor(this.plugin, observer, location, title);
            this.holograms.put(observer.getUniqueId(), new Hologram(observer, entityId));
        } catch (HologramException e) {
            this.plugin.getLogger().log(Level.WARNING, "cannot create hologram instance", e);
        }
    }

    /**
     * Destroys a specific hologram by despawning its entity.
     *
     * @param hologram hologram instance to destroy
     */
    private void destroyHologram(Hologram hologram) {
        try {
            this.armorStandAdapter.destroyArmorStandFor(hologram.getObserver(), hologram.getEntityId());
        } catch (HologramException e) {
            this.plugin.getLogger().log(Level.WARNING, "cannot remove hologram instance", e);
        }
    }

}

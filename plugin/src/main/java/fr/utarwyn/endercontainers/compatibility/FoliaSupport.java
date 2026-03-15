package fr.utarwyn.endercontainers.compatibility;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

/**
 * Compatibility layer for Folia scheduler support.
 * Provides abstraction over scheduler differences between Spigot/Paper and Folia.
 *
 * @author Utarwyn
 * @since 2.3.0
 */
public class FoliaSupport {

    private static final boolean IS_FOLIA;

    static {
        // Check if we're running on Folia
        IS_FOLIA = checkFolia();
    }

    /**
     * Checks if the server is running Folia.
     */
    private static boolean checkFolia() {
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    /**
     * Check if the server is running Folia.
     */
    public static boolean isFolia() {
        return IS_FOLIA;
    }

    /**
     * Run a task asynchronously in a way compatible with both Folia and Spigot/Paper.
     *
     * @param plugin the plugin instance
     * @param task   the task to run
     */
    public static void runTaskAsynchronously(Plugin plugin, Runnable task) {
        if (IS_FOLIA) {
            // On Folia, use direct threading for async tasks
            new Thread(task).start();
        } else {
            // On Spigot/Paper, use traditional scheduler
            Bukkit.getScheduler().runTaskAsynchronously(plugin, task);
        }
    }

    /**
     * Run a task on the main thread in a way compatible with both Folia and Spigot/Paper.
     *
     * @param plugin the plugin instance
     * @param task   the task to run
     */
    public static void runTaskOnMainThread(Plugin plugin, Runnable task) {
        if (IS_FOLIA) {
            // On Folia, use GlobalRegionScheduler for main thread execution
            try {
                Object globalRegionScheduler = Bukkit.getServer().getClass()
                        .getMethod("getGlobalRegionScheduler")
                        .invoke(Bukkit.getServer());

                globalRegionScheduler.getClass()
                        .getMethod("execute", Plugin.class, Runnable.class)
                        .invoke(globalRegionScheduler, plugin, task);
            } catch (Exception e) {
                // Fallback to direct execution if reflection fails
                task.run();
            }
        } else {
            // On Spigot/Paper, use traditional scheduler
            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, task);
        }
    }

}

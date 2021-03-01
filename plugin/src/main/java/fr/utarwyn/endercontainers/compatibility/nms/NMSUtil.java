package fr.utarwyn.endercontainers.compatibility.nms;

import fr.utarwyn.endercontainers.compatibility.ServerVersion;

import java.lang.reflect.Field;

/**
 * Base class to perform reflection things on current server net classes.
 *
 * @author Utarwyn
 * @since 2.2.0
 */
public abstract class NMSUtil {

    /**
     * Package where NMS classes are stored
     */
    private static final String NMS_PACKAGE;

    /**
     * Package where Craftbukkit classes are stored
     */
    private static final String CRAFTBUKKIT_PACKAGE;

    /**
     * Field whiches stores enabling state of asynchronous tasks in Spigot
     */
    private static Field spigotAsyncCatcherEnabled;

    static {
        String version = ServerVersion.getBukkitVersion();
        NMS_PACKAGE = "net.minecraft.server." + version;
        CRAFTBUKKIT_PACKAGE = "org.bukkit.craftbukkit." + version;

        try {
            Class<?> asyncCatcher = Class.forName("org.spigotmc.AsyncCatcher");
            spigotAsyncCatcherEnabled = asyncCatcher.getField("enabled");
        } catch (ReflectiveOperationException ignored) {
            // Ignore this.. we don't use Spigot!
        }
    }

    /**
     * Utility class.
     */
    protected NMSUtil() {
        // Not implemented
    }

    /**
     * Get a Craftbukkit class.
     *
     * @param className name of the class to get
     * @return the Craftbukkit class found by the name
     * @throws ClassNotFoundException thrown if the class is not found
     */
    protected static Class<?> getCraftbukkitClass(String className)
            throws ClassNotFoundException {
        return Class.forName(CRAFTBUKKIT_PACKAGE + "." + className);
    }

    /**
     * Get an internal net Minecraft Server class.
     *
     * @param className name of the class to get
     * @return the internal class found by the name
     * @throws ClassNotFoundException thrown if the class is not found
     */
    protected static Class<?> getNMSClass(String className)
            throws ClassNotFoundException {
        return Class.forName(NMS_PACKAGE + "." + className);
    }

    /**
     * Checks if usage of asynchronous tasks is disabled.
     * This can occurs when using Spigot with the restart command for example.
     *
     * @return true if usage asynchronous tasks disabled
     */
    public static boolean isAsyncDisabled() {
        try {
            return spigotAsyncCatcherEnabled != null && !((boolean) spigotAsyncCatcherEnabled.get(null));
        } catch (Exception e) {
            return false;
        }
    }
}

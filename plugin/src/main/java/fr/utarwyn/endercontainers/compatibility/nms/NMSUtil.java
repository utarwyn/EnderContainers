package fr.utarwyn.endercontainers.compatibility.nms;

import fr.utarwyn.endercontainers.compatibility.ServerVersion;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Base class to perform reflection things on current server net classes.
 *
 * @author Utarwyn
 * @since 2.2.0
 */
public abstract class NMSUtil {

    /**
     * Package where NMS classes are stored. Used before 1.17
     */
    private static final String NMS_PACKAGE;

    /**
     * Package where Minecraft classes are stored. Used in 1.17+
     */
    private static final String MC_PACKAGE;

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

        // Using remapped source code of Minecraft server? 1.17+
        MC_PACKAGE = ServerVersion.isNewerThan(ServerVersion.V1_16) ? "net.minecraft." : null;
        NMS_PACKAGE = "net.minecraft.server." + version + ".";
        CRAFTBUKKIT_PACKAGE = "org.bukkit.craftbukkit." + version + ".";

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
        return Class.forName(CRAFTBUKKIT_PACKAGE + className);
    }

    /**
     * Get an internal net Minecraft Server class.
     *
     * @param className   name of the class
     * @param package1_17 name of the package to prefix before, for 1.17+
     * @return the internal class found by the name
     * @throws ClassNotFoundException thrown if the class is not found
     */
    protected static Class<?> getNMSClass(String className, String package1_17)
            throws ClassNotFoundException {
        return Class.forName((MC_PACKAGE != null ? MC_PACKAGE + package1_17 + "." : NMS_PACKAGE) + className);
    }

    /**
     * Get an internal net Minecraft Server method with a name changes.
     * Usefull from Minecraft 1.18 because of minifier.
     *
     * @param clazz          class where the method is located
     * @param name           name used before Minecraft 1.18
     * @param name1_18       name introduced with Minecraft 1.18
     * @param parameterTypes parameter types
     * @return located method
     * @throws NoSuchMethodException thrown if method has not been found
     */
    protected static Method getNMSDynamicMethod(Class<?> clazz, String name, String name1_18, Class<?>... parameterTypes)
            throws NoSuchMethodException {
        return clazz.getMethod(ServerVersion.isNewerThan(ServerVersion.V1_17) ? name1_18 : name, parameterTypes);
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

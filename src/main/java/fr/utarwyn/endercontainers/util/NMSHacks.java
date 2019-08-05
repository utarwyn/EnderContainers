package fr.utarwyn.endercontainers.util;

import fr.utarwyn.endercontainers.EnderContainers;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.UUID;
import java.util.logging.Level;

/**
 * Utility class used to load and save offline player data.
 *
 * @author Utarwyn
 * @since 1.0.5
 */
public class NMSHacks {

    /**
     * Stores the main package of the net minecraft server (NMS)
     */
    private static final String NMS_PACKAGE;

    static {
        String craftbukkitPackage = Bukkit.getServer().getClass().getPackage().getName() + ".";
        NMS_PACKAGE = craftbukkitPackage.replace("org.bukkit.craftbukkit", "net.minecraft.server");
    }

    /**
     * Utility class!
     */
    private NMSHacks() {

    }

    /**
     * Main method of the class, returns a Player object for an offline player.
     *
     * @param playerName Playername used to load the offline player data
     * @param uuid       UUID used to load the offline player data
     * @return Generated Player object
     */
    public static Player getPlayerObjectOfOfflinePlayer(String playerName, UUID uuid) {
        try {
            boolean useGameProfile = isServerPost16();
            Object minecraftServer = getMinecraftServerInstance();

            Class<?> entityPlayerClass = getNMSClass("EntityPlayer");
            Class<?> minecraftServerClass = getNMSClass("MinecraftServer");
            Class<?> worldClass = getNMSClass("World");
            Class<?> gameProfileClass = null;

            if (useGameProfile) {
                gameProfileClass = getGameProfileClass();
            }

            Class<?> interactManagerClass = getNMSClass("PlayerInteractManager");
            assert entityPlayerClass != null;

            Constructor<?> entityPlayerCstr = entityPlayerClass.getDeclaredConstructor(
                    minecraftServerClass, getNMSClass("WorldServer"),
                    useGameProfile ? gameProfileClass : String.class, interactManagerClass
            );

            Constructor<?> gameProfileCstr = null;
            if (useGameProfile) {
                assert gameProfileClass != null;
                gameProfileCstr = gameProfileClass.getDeclaredConstructor(UUID.class, String.class);
            }

            assert interactManagerClass != null;
            Constructor<?> interactManagerCstr = interactManagerClass.getDeclaredConstructor(worldClass);

            Object gameProfile = null;
            if (useGameProfile) {
                gameProfile = gameProfileCstr.newInstance(uuid, playerName);
            }

            Object playerInteractManager = interactManagerCstr.newInstance(getWorldServer0());
            Object entityPlayer = entityPlayerCstr.newInstance(
                    minecraftServer, getWorldServer0(),
                    useGameProfile ? gameProfile : playerName,
                    playerInteractManager
            );

            Method getBukkitEntityMethod = entityPlayerClass.getDeclaredMethod("getBukkitEntity");

            return (Player) getBukkitEntityMethod.invoke(entityPlayer);
        } catch (NoSuchMethodException | InstantiationException
                | IllegalAccessException | InvocationTargetException e) {
            EnderContainers.getInstance().getLogger().log(
                    Level.SEVERE, "Cannot access the offline player profile", e);
            return null;
        }
    }

    private static boolean isServerPost16() {
        try {
            Bukkit.getServer().getServerIcon();
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    private static Object getWorldServer0() {
        try {
            return reflectWorldServer0();
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            EnderContainers.getInstance().getLogger().log(
                    Level.SEVERE, "Cannot reflect the world server", e);
            return null;
        }
    }

    private static Object reflectWorldServer0() throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        Object serverInstance = getMinecraftServerInstance();
        assert serverInstance != null;

        // TODO This method does not work on 1.13.1 version anymore
        // Not a method inside DedicatedServer, use getMethod
        return serverInstance.getClass().getMethod("getWorldServer", int.class)
                .invoke(getMinecraftServerInstance(), 0);
    }

    private static Object getMinecraftServerInstance() {
        try {
            return reflectMinecraftServerInstance();
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            EnderContainers.getInstance().getLogger().log(
                    Level.SEVERE, "Cannot reflect the Minecraft server instance", e);
            return null;
        }
    }

    private static Object reflectMinecraftServerInstance() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        Class<?> dedicatedServerClass = getNMSClass("DedicatedServer");
        assert dedicatedServerClass != null;

        // Use getMethod instead of getDeclaredMethod, because the getServer method is declared in MinecraftServer, not DedicatedServer
        return dedicatedServerClass.getMethod("getServer").invoke(null);
    }

    private static Class<?> getGameProfileClass() {
        try {
            return Class.forName("com.mojang.authlib.GameProfile");
        } catch (ClassNotFoundException e) {
            EnderContainers.getInstance().getLogger().log(
                    Level.SEVERE, "Cannot find the game profile class", e);
            return null;
        }
    }

    private static Class<?> getNMSClass(String className) {
        try {
            return Class.forName(NMS_PACKAGE + className);
        } catch (ClassNotFoundException e) {
            EnderContainers.getInstance().getLogger().log(
                    Level.SEVERE, "Cannot find a Minecraft server class", e);
            return null;
        }
    }

}

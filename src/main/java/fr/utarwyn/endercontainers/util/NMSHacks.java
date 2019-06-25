package fr.utarwyn.endercontainers.util;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.UUID;

/**
 * Utility class used to load and save offline player data.
 *
 * @author Utarwyn
 * @since 1.0.5
 */
public class NMSHacks {

    /**
     * Stores the main package of the craftbukkit server
     */
    private static String craftbukkitPackage;

    /**
     * Stores the main package of the net minecraft server (NMS)
     */
    private static String nmsPackage;

    static {
        craftbukkitPackage = Bukkit.getServer().getClass().getPackage().getName() + ".";
        nmsPackage = craftbukkitPackage.replace("org.bukkit.craftbukkit", "net.minecraft.server");
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

            Class<?> class_EntityPlayer = getNMSClass("EntityPlayer");
            Class<?> class_MinecraftServer = getNMSClass("MinecraftServer");
            Class<?> class_World = getNMSClass("World");

            Class<?> class_GameProfile = null;
            if (useGameProfile) {
                class_GameProfile = getGameProfileClass();
            }

            Class<?> class_PlayerInteractManager = getNMSClass("PlayerInteractManager");

            assert class_EntityPlayer != null;
            Constructor<?> constructor_EntityPlayer = class_EntityPlayer.getDeclaredConstructor(class_MinecraftServer, getNMSClass("WorldServer"), useGameProfile ? class_GameProfile : String.class,
                    class_PlayerInteractManager);

            Constructor<?> constructor_GameProfile = null;
            if (useGameProfile) {
                assert class_GameProfile != null;
                constructor_GameProfile = class_GameProfile.getDeclaredConstructor(UUID.class, String.class);
            }

            assert class_PlayerInteractManager != null;
            Constructor<?> constructor_PlayerInteractManager = class_PlayerInteractManager.getDeclaredConstructor(class_World);

            Object gameProfile = null;
            if (useGameProfile) {
                gameProfile = constructor_GameProfile.newInstance(uuid, playerName);
            }

            Object playerInteractManager = constructor_PlayerInteractManager.newInstance(getWorldServer0());

            Object entityPlayer = constructor_EntityPlayer.newInstance(minecraftServer, getWorldServer0(), useGameProfile ? gameProfile : playerName,
                    playerInteractManager);

            Method method_getBukkitEntity = class_EntityPlayer.getDeclaredMethod("getBukkitEntity");

            return (Player) method_getBukkitEntity.invoke(entityPlayer);
        } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException
                | InvocationTargetException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static boolean isServerPost16() {
        try {
            Bukkit.getServer().getServerIcon();
            return true;
        } catch (Throwable t) {
            return false;
        }
    }

    private static Object getWorldServer0() {
        try {
            return reflectWorldServer0();
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static Object reflectWorldServer0() throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
        Object serverInstance = getMinecraftServerInstance();
        assert serverInstance != null;

        // TODO This method does not work on 1.13.1 version anymore
        return serverInstance.getClass().getMethod("getWorldServer", int.class).invoke(getMinecraftServerInstance(), 0); //Not a method inside DedicatedServer, use getMethod
    }

    private static Object getMinecraftServerInstance() {
        try {
            return reflectMinecraftServerInstance();
        } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static Object reflectMinecraftServerInstance() throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        Class<?> class_DedicatedServer = getNMSClass("DedicatedServer");
        assert class_DedicatedServer != null;
        Method method_getServer = class_DedicatedServer.getMethod("getServer"); //Use getMethod instead of getDeclaredMethod, because the getServer method is declared in MinecraftServer, not DedicatedServer

        return method_getServer.invoke(null); //Forgot about this: reflection's javadocs say that if it is a static method, then parse null to the "obj" argument.
    }

    private static Class<?> getGameProfileClass() {
        try {
            return Class.forName("com.mojang.authlib.GameProfile");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static Class<?> getNMSClass(String className) {
        try {
            return Class.forName(nmsPackage + className);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

}

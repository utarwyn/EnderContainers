package fr.utarwyn.endercontainers.compatibility.nms;

import fr.utarwyn.endercontainers.compatibility.ServerVersion;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.UUID;

/**
 * This class is used to perform reflection things
 * on server classes to deal with offline players.
 *
 * @author Utarwyn
 * @since 2.2.0
 */
public class NMSPlayerUtil extends NMSUtil {

    /**
     * Singleton instance of the utility class.
     */
    private static NMSPlayerUtil instance;

    private final Constructor<?> entityPlayerConstructor;

    private final Constructor<?> gameProfileContructor;

    private final Method getBukkitEntityMethod;

    private final Object minecraftServer;

    private final Object worldServer;

    private final String methodGetWorldServer;

    private Object playerInteractManager;

    /**
     * Constructs the utility class.
     *
     * @throws ReflectiveOperationException thrown if cannot instanciate NMS objects
     */
    private NMSPlayerUtil() throws ReflectiveOperationException {
        Class<?> entityPlayerClass = getNMSClass("EntityPlayer", "server.level");
        Class<?> minecraftServerClass = getNMSClass("MinecraftServer", "server");
        Class<?> gameProfileClass = Class.forName("com.mojang.authlib.GameProfile");
        Class<?> worldServerClass = getNMSClass("WorldServer", "server.level");

        // 1.18+ :: New method names
        methodGetWorldServer = ServerVersion.isNewerThan(ServerVersion.V1_17) ? "a" : "getWorldServer";

        gameProfileContructor = gameProfileClass.getDeclaredConstructor(UUID.class, String.class);
        minecraftServer = minecraftServerClass.getMethod("getServer").invoke(null);
        getBukkitEntityMethod = entityPlayerClass.getDeclaredMethod("getBukkitEntity");
        worldServer = this.prepareWorldServer(minecraftServerClass);

        // 1.17+ :: we do not have to pass PlayerInteractManager to entity player constructor
        if (ServerVersion.isNewerThan(ServerVersion.V1_16)) {
            entityPlayerConstructor = entityPlayerClass.getDeclaredConstructor(
                    minecraftServerClass, worldServerClass, gameProfileClass
            );
        } else {
            Class<?> playerInteractManagerClass = getNMSClass("PlayerInteractManager", "server.level");
            entityPlayerConstructor = entityPlayerClass.getDeclaredConstructor(
                    minecraftServerClass, worldServerClass,
                    gameProfileClass, playerInteractManagerClass
            );

            // Prepare the PlayerInteractManager
            playerInteractManager = this.preparePlayerInteractManager(worldServerClass, playerInteractManagerClass);
        }
    }

    /**
     * Retrieves or creates an instance of the utility class.
     *
     * @return utility class instance
     * @throws ReflectiveOperationException thrown if cannot instanciate NMS objects
     */
    public static NMSPlayerUtil get() throws ReflectiveOperationException {
        if (instance == null) {
            instance = new NMSPlayerUtil();
        }
        return instance;
    }

    /**
     * Load the profile of an offline player to manipulate its enderchest.
     *
     * @param offline offline player
     * @return base object of the offline player
     * @throws ReflectiveOperationException thrown if the player profile cannot be decoded
     */
    public Player loadPlayer(OfflinePlayer offline) throws ReflectiveOperationException {
        if (!offline.hasPlayedBefore()) {
            return null;
        }

        Object gameProfile = gameProfileContructor.newInstance(offline.getUniqueId(), offline.getName());
        Object entityPlayer;

        // 1.17+ :: we do not have to pass PlayerInteractManager to entity player constructor
        if (playerInteractManager == null) {
            entityPlayer = entityPlayerConstructor.newInstance(minecraftServer, worldServer, gameProfile);
        } else {
            entityPlayer = entityPlayerConstructor.newInstance(minecraftServer, worldServer, gameProfile, playerInteractManager);
        }

        Player player = (Player) getBukkitEntityMethod.invoke(entityPlayer);

        if (player != null) {
            player.loadData();
        }

        return player;
    }

    private Object getWorldServer116(Object minecraftServer) throws ReflectiveOperationException {
        Class<?> minecraftServerClass = getNMSClass("MinecraftServer", "server");
        Class<?> genericResourceKey = getNMSClass("ResourceKey", "resources");
        Class<?> minecraftKey = getNMSClass("MinecraftKey", "resources");

        Method constructResourceKey = genericResourceKey.getDeclaredMethod("a", minecraftKey, minecraftKey);
        constructResourceKey.setAccessible(true);

        Constructor<?> minecraftKeyConstructor = minecraftKey.getConstructor(String.class);
        Object overworldKey = minecraftKeyConstructor.newInstance("overworld");
        Object dimensionKey = minecraftKeyConstructor.newInstance("dimension");
        Object resourceKey = constructResourceKey.invoke(null, dimensionKey, overworldKey);
        constructResourceKey.setAccessible(false);

        Method getWorldServer = minecraftServerClass.getDeclaredMethod(methodGetWorldServer, genericResourceKey);
        return getWorldServer.invoke(minecraftServer, resourceKey);
    }

    private Object prepareWorldServer(Class<?> minecraftServerClass) throws ReflectiveOperationException {
        Object server;
        if (ServerVersion.isNewerThan(ServerVersion.V1_8)) {
            if (ServerVersion.isNewerThan(ServerVersion.V1_15)) {
                server = getWorldServer116(minecraftServer);
            } else {
                Class<?> dimensionManagerClass = getNMSClass("DimensionManager", null);
                Object dimension = dimensionManagerClass.getDeclaredField("OVERWORLD").get(null);
                Method method = minecraftServerClass.getMethod(methodGetWorldServer, dimensionManagerClass);
                server = method.invoke(minecraftServer, dimension);
            }
        } else {
            Method method = minecraftServerClass.getMethod(methodGetWorldServer, int.class);
            server = method.invoke(minecraftServer, 0);
        }

        return server;
    }

    private Object preparePlayerInteractManager(
            Class<?> worldServerClass, Class<?> playerInteractManagerClass
    ) throws ReflectiveOperationException {
        Object manager;
        if (ServerVersion.isNewerThan(ServerVersion.V1_8)) {
            manager = playerInteractManagerClass.getDeclaredConstructor(worldServerClass).newInstance(worldServer);
        } else {
            Class<?> worldClass = getNMSClass("World", null);
            manager = playerInteractManagerClass.getDeclaredConstructor(worldClass).newInstance(worldServer);
        }

        return manager;
    }

}

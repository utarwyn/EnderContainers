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

    private static final String GET_WORLD_SERVER_METHOD = "getWorldServer";

    /**
     * Singleton instance of the utility class.
     */
    private static NMSPlayerUtil instance;

    private final Constructor<?> entityPlayerConstructor;

    private final Constructor<?> gameProfileContructor;

    private final Method getBukkitEntityMethod;

    private final Object minecraftServer;

    private final Object worldServer;

    private final Object playerInteractManager;

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
        Class<?> playerInteractManagerClass = getNMSClass("PlayerInteractManager", "server.level");

        gameProfileContructor = gameProfileClass.getDeclaredConstructor(UUID.class, String.class);
        entityPlayerConstructor = entityPlayerClass.getDeclaredConstructor(
                minecraftServerClass, worldServerClass,
                gameProfileClass, playerInteractManagerClass
        );
        minecraftServer = minecraftServerClass.getMethod("getServer").invoke(null);
        getBukkitEntityMethod = entityPlayerClass.getDeclaredMethod("getBukkitEntity");

        // Prepare a PlayerInteractManager
        if (ServerVersion.isNewerThan(ServerVersion.V1_8)) {
            if (ServerVersion.isNewerThan(ServerVersion.V1_15)) {
                worldServer = getWorldServer116(minecraftServer);
            } else {
                Class<?> dimensionManagerClass = getNMSClass("DimensionManager", "world.level.dimension");
                Object dimension = dimensionManagerClass.getDeclaredField("OVERWORLD").get(null);
                Method method = minecraftServerClass.getMethod(GET_WORLD_SERVER_METHOD, dimensionManagerClass);
                worldServer = method.invoke(minecraftServer, dimension);
            }

            playerInteractManager = playerInteractManagerClass.getDeclaredConstructor(worldServerClass).newInstance(worldServer);
        } else {
            Class<?> worldClass = getNMSClass("World", "world.level");
            Method method = minecraftServerClass.getMethod(GET_WORLD_SERVER_METHOD, int.class);

            worldServer = method.invoke(minecraftServer, 0);
            playerInteractManager = playerInteractManagerClass.getDeclaredConstructor(worldClass).newInstance(worldServer);
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
        Object entityPlayer = entityPlayerConstructor.newInstance(minecraftServer, worldServer, gameProfile, playerInteractManager);
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

        Method getWorldServer = minecraftServerClass.getDeclaredMethod(GET_WORLD_SERVER_METHOD, genericResourceKey);
        return getWorldServer.invoke(minecraftServer, resourceKey);
    }

}

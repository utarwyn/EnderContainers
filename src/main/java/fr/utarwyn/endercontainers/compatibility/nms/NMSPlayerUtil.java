package fr.utarwyn.endercontainers.compatibility.nms;

import fr.utarwyn.endercontainers.EnderContainers;
import fr.utarwyn.endercontainers.compatibility.ServerVersion;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.UUID;
import java.util.logging.Level;

/**
 * This class is used to perform reflection things
 * on server classes to deal with offline players.
 *
 * @author Utarwyn
 * @since 2.2.0
 */
public class NMSPlayerUtil extends NMSUtil {

    private static Constructor<?> entityPlayerConstructor;

    private static Constructor<?> gameProfileContructor;

    private static Method getBukkitEntityMethod;

    private static Object minecraftServer;

    private static Object worldServer;

    private static Object playerInteractManager;

    static {
        try {
            Class<?> entityPlayerClass = getNMSClass("EntityPlayer");
            Class<?> minecraftServerClass = getNMSClass("MinecraftServer");
            Class<?> gameProfileClass = Class.forName("com.mojang.authlib.GameProfile");
            Class<?> worldServerClass = getNMSClass("WorldServer");
            Class<?> playerInteractManagerClass = getNMSClass("PlayerInteractManager");

            gameProfileContructor = gameProfileClass.getDeclaredConstructor(UUID.class, String.class);
            entityPlayerConstructor = entityPlayerClass.getDeclaredConstructor(
                    minecraftServerClass, worldServerClass,
                    gameProfileClass, playerInteractManagerClass
            );
            minecraftServer = minecraftServerClass.getMethod("getServer").invoke(null);
            getBukkitEntityMethod = entityPlayerClass.getDeclaredMethod("getBukkitEntity");

            // Prepare a PlayerInteractManager
            if (ServerVersion.isNewerThan(ServerVersion.V1_8)) {
                Class<?> dimensionManagerClass = getNMSClass("DimensionManager");
                Object dimension = dimensionManagerClass.getDeclaredField("OVERWORLD").get(null);
                Method method = minecraftServerClass.getMethod("getWorldServer", dimensionManagerClass);

                worldServer = method.invoke(minecraftServer, dimension);
                playerInteractManager = playerInteractManagerClass.getDeclaredConstructor(worldServerClass).newInstance(worldServer);
            } else {
                Class<?> worldClass = getNMSClass("World");
                Method method = minecraftServerClass.getMethod("getWorldServer", int.class);

                worldServer = method.invoke(minecraftServer, 0);
                playerInteractManager = playerInteractManagerClass.getDeclaredConstructor(worldClass).newInstance(worldServer);
            }
        } catch (ReflectiveOperationException e) {
            EnderContainers.getInstance().getLogger().log(Level.SEVERE,
                    "Cannot initialize the NMS player utility class", e);
        }
    }

    /**
     * Utility class.
     */
    private NMSPlayerUtil() {
        // Not implemented
    }

    /**
     * Load the profile of an offline player to manipulate its enderchest.
     *
     * @param offline offline player
     * @return base object of the offline player
     * @throws ReflectiveOperationException thrown if the player profile cannot be decoded
     */
    public static Player loadPlayer(OfflinePlayer offline) throws ReflectiveOperationException {
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

}

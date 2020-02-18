package fr.utarwyn.endercontainers.compatibility.nms;

import fr.utarwyn.endercontainers.EnderContainers;
import fr.utarwyn.endercontainers.compatibility.ServerVersion;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.logging.Level;

/**
 * This class is used to perform reflection things
 * on server net classes to spawn holograms for all versions.
 *
 * @author Utarwyn
 * @since 2.2.0
 */
public class NMSHologramUtil extends NMSUtil {

    /**
     * Reflection NMS Entity class
     */
    private static Class<?> entityClass;

    /**
     * Reflection NMS Packet class
     */
    private static Class<?> packetClass;

    /**
     * Reflection CraftWorld class
     */
    private static Class<?> craftWorldClass;

    /**
     * CraftChatMessages class for 1.13+ holograms
     */
    private static Class<?> chatMessageClass;

    /**
     * IChatBaseComponent class for 1.13+ holograms
     */
    private static Class<?> chatBaseComponentClass;

    /**
     * EntityTypes class for 1.14+ holograms
     */
    private static Class<?> entityTypesClass;

    /**
     * Constructor of the ArmorStand class
     */
    private static Constructor<?> armorStandConstructor;

    /**
     * Constructor for the spawn packet
     */
    private static Constructor<?> spawnPacketConstructor;

    /**
     * Metadata Packet constructor for 1.15+ holograms
     */
    private static Constructor<?> metadataPacketConstructor;

    /**
     * Constructor for the destroy packet
     */
    private static Constructor<?> destroyPacketConstructor;

    static {
        try {
            Class<?> worldClass = getNMSClass("World");
            Class<?> armorStandClass = getNMSClass("EntityArmorStand");
            Class<?> destroyPacketClass = getNMSClass("PacketPlayOutEntityDestroy");
            Class<?> spawnPacketClass = getNMSClass("PacketPlayOutSpawnEntityLiving");

            packetClass = getNMSClass("Packet");
            entityClass = getNMSClass("Entity");
            craftWorldClass = getCraftbukkitClass("CraftWorld");

            spawnPacketConstructor = spawnPacketClass.getConstructor(getNMSClass("EntityLiving"));
            destroyPacketConstructor = destroyPacketClass.getConstructor(int[].class);

            // 1.13+ :: text to display needed to be converted to a IChatBaseComponent component
            if (ServerVersion.isNewerThan(ServerVersion.V1_12)) {
                chatMessageClass = getCraftbukkitClass("util.CraftChatMessage");
                chatBaseComponentClass = getNMSClass("IChatBaseComponent");
            }

            // 1.14+ :: the living entity constructor must be called with the entity type
            if (ServerVersion.isNewerThan(ServerVersion.V1_13)) {
                entityTypesClass = getNMSClass("EntityTypes");
                armorStandConstructor = armorStandClass.getConstructor(entityTypesClass, worldClass);
            } else {
                armorStandConstructor = armorStandClass.getConstructor(worldClass);
            }

            // 1.15+ :: entity metadatas have to be sent in separate packet
            if (ServerVersion.isNewerThan(ServerVersion.V1_14)) {
                Class<?> packetClass = getNMSClass("PacketPlayOutEntityMetadata");
                Class<?> dataWatcherClass = getNMSClass("DataWatcher");
                metadataPacketConstructor = packetClass.getConstructor(int.class, dataWatcherClass, boolean.class);
            }
        } catch (ReflectiveOperationException e) {
            EnderContainers.getInstance().getLogger().log(Level.SEVERE, "Cannot initialize the hologram", e);
        }
    }

    /**
     * Utility class.
     */
    private NMSHologramUtil() {
        // Not implemented
    }

    /**
     * Spawn a custom hologram (text, location) for a player.
     *
     * @param location location of the hologram
     * @param text     text to display with the hologram
     * @param observer observer of the hologram
     * @return Entity identifier of the spawned hologram
     * @throws ReflectiveOperationException error with reflection
     * @see <a href="https://wiki.vg/Protocol#Spawn_Mob">Protocol for 1.8 to 1.14</a>
     * @see <a href="https://wiki.vg/Pre-release_protocol#Spawn_Mob">Protocol for 1.15+</a>
     */
    public static int spawnHologram(Location location, String text, Player observer) throws ReflectiveOperationException {
        // First, we need to generate the fake armorstand
        Object entity = createHologramEntity(location.getWorld(), location.getX(), location.getY(), location.getZ(), text);
        int entityId = (int) entityClass.getDeclaredMethod("getId").invoke(entity);

        // Send the spawn packet for 1.8+
        sendPacket(observer, spawnPacketConstructor.newInstance(entity));

        // Send the metadata packet for 1.15+
        if (ServerVersion.isNewerThan(ServerVersion.V1_14)) {
            Method getDataWatcher = entityClass.getDeclaredMethod("getDataWatcher");
            Object metadataPacket = metadataPacketConstructor.newInstance(entityId, getDataWatcher.invoke(entity), false);
            sendPacket(observer, metadataPacket);
        }

        return entityId;
    }

    /**
     * Destroy a specific entity for a player by its identifier.
     *
     * @param entityId identifier of the entity to destroy
     * @param observer player concerned by the deletion
     * @throws ReflectiveOperationException error with reflection
     */
    public static void destroyEntity(int entityId, Player observer) throws ReflectiveOperationException {
        sendPacket(observer, destroyPacketConstructor.newInstance((Object) new int[]{entityId}));
    }

    /**
     * Send a packet to the player linked to the hologram.
     *
     * @param player the player who has to receive the packet
     * @param packet the packet to send
     * @throws ReflectiveOperationException error with reflection
     */
    private static void sendPacket(Player player, Object packet) throws ReflectiveOperationException {
        Method getHandle = player.getClass().getMethod("getHandle");
        Object entityPlayer = getHandle.invoke(player);
        Object pConnection = entityPlayer.getClass().getField("playerConnection").get(entityPlayer);
        Method sendMethod = pConnection.getClass().getMethod("sendPacket", packetClass);

        sendMethod.invoke(pConnection, packet);
    }

    /**
     * Create the entity used to display an hologram.
     *
     * @param w    World
     * @param x    X coordinate
     * @param y    Y coordinate
     * @param z    Z coordinate
     * @param text Text to display
     * @return The spawn packet object
     * @throws ReflectiveOperationException error with reflection
     */
    private static Object createHologramEntity(World w, double x, double y, double z, String text) throws ReflectiveOperationException {
        Object craftWorldObj = craftWorldClass.cast(w);
        Method getHandleMethod = craftWorldObj.getClass().getMethod("getHandle");

        Object entityObject;

        // In 1.14+ versions, we need to specify a type to create an instance of an ArmorStand.
        if (ServerVersion.isNewerThan(ServerVersion.V1_13)) {
            Object armorStandType = entityTypesClass.getField("ARMOR_STAND").get(null);
            entityObject = armorStandConstructor.newInstance(armorStandType, getHandleMethod.invoke(craftWorldObj));
        } else {
            entityObject = armorStandConstructor.newInstance(getHandleMethod.invoke(craftWorldObj));
        }

        // In 1.13+ versions, the custom name must be a IChatBaseComponent component
        if (ServerVersion.isNewerThan(ServerVersion.V1_12)) {
            Method fromStringOrNullMethod = chatMessageClass.getMethod("fromStringOrNull", String.class);
            Object chatComponent = fromStringOrNullMethod.invoke(null, text);

            Method setCustomName = entityObject.getClass().getMethod("setCustomName", chatBaseComponentClass);
            setCustomName.invoke(entityObject, chatComponent);
        } else {
            Method setCustomName = entityObject.getClass().getMethod("setCustomName", String.class);
            setCustomName.invoke(entityObject, text);
        }

        Method setCustomNameVisible = entityClass.getMethod("setCustomNameVisible", boolean.class);
        setCustomNameVisible.invoke(entityObject, true);

        if (ServerVersion.isOlderThan(ServerVersion.V1_10)) {   // 1.8 / 1.9
            Method setGravity = entityObject.getClass().getMethod("setGravity", boolean.class);
            setGravity.invoke(entityObject, false);
        } else {                                                // 1.10+
            Method setNoGravity = entityObject.getClass().getMethod("setNoGravity", boolean.class);
            setNoGravity.invoke(entityObject, true);
        }

        Method setLocation = entityObject.getClass().getMethod("setLocation", double.class, double.class, double.class, float.class, float.class);
        setLocation.invoke(entityObject, x, y, z, 0.0F, 0.0F);

        Method setInvisible = entityObject.getClass().getMethod("setInvisible", boolean.class);
        setInvisible.invoke(entityObject, true);

        return entityObject;
    }

}

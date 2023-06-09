package fr.utarwyn.endercontainers.compatibility.nms;

import fr.utarwyn.endercontainers.compatibility.ServerVersion;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;

/**
 * This class is used to perform reflection things
 * on server net classes to spawn holograms for all versions.
 *
 * @author Utarwyn
 * @since 2.2.0
 */
public class NMSHologramUtil extends NMSUtil {

    /**
     * Singleton instance of the utility class.
     */
    private static NMSHologramUtil instance;

    /**
     * Reflection NMS Entity class
     */
    private final Class<?> entityClass;

    /**
     * Reflection NMS Packet class
     */
    private final Class<?> packetClass;

    /**
     * Reflection CraftWorld class
     */
    private final Class<?> craftWorldClass;

    /**
     * CraftChatMessages class for 1.13+ holograms
     */
    private final Class<?> chatMessageClass;

    /**
     * IChatBaseComponent class for 1.13+ holograms
     */
    private final Class<?> chatBaseComponentClass;

    /**
     * Constructor of the ArmorStand class
     */
    private final Constructor<?> armorStandConstructor;

    /**
     * Constructor for the spawn packet
     */
    private final Constructor<?> spawnPacketConstructor;

    /**
     * Metadata Packet constructor for 1.15+ holograms
     */
    private final Constructor<?> metadataPacketConstructor;

    /**
     * Constructor for the destroy packet
     */
    private Constructor<?> destroyPacketConstructor;

    /**
     * Stores if constructor for the destroy packet uses only one parameter
     */
    private boolean destroyPacketConstructorOneInt;

    /**
     * EntityTypes invokation method for 1.14+ holograms
     */
    private final Method entityTypesInvokationMethod;

    /**
     * Field with a playerConnection
     */
    private final Field playerConnectionField;

    /**
     * Utility class.
     */
    private NMSHologramUtil() throws ReflectiveOperationException {
        Class<?> worldClass = getNMSClass("World", "world.level");
        Class<?> armorStandClass = getNMSClass("EntityArmorStand", "world.entity.decoration");
        Class<?> entityPlayerClass = getNMSClass("EntityPlayer", "server.level");
        Class<?> destroyPacketClass = getNMSClass("PacketPlayOutEntityDestroy", "network.protocol.game");

        // 1.19+ :: PacketPlayOutSpawnEntityLiving has been renamed PacketPlayOutSpawnEntity
        String spawnPacketClassName = ServerVersion.isNewerThan(ServerVersion.V1_18)
                ? "PacketPlayOutSpawnEntity" : "PacketPlayOutSpawnEntityLiving";
        Class<?> spawnPacketClass = getNMSClass(spawnPacketClassName, "network.protocol.game");

        this.packetClass = getNMSClass("Packet", "network.protocol");
        this.entityClass = getNMSClass("Entity", "world.entity");
        this.craftWorldClass = getCraftbukkitClass("CraftWorld");

        // 1.19.3+ :: spawn packet constructor use Entity instead of LivingEntity
        if (ServerVersion.isNewerThan(ServerVersion.V1_19)) {
            this.spawnPacketConstructor = spawnPacketClass.getConstructor(this.entityClass);
        } else {
            this.spawnPacketConstructor = spawnPacketClass.getConstructor(getNMSClass("EntityLiving", "world.entity"));
        }

        // 1.17+ :: try to use only one int in packet constructor parameters
        try {
            this.destroyPacketConstructor = destroyPacketClass.getConstructor(int[].class);
            this.destroyPacketConstructorOneInt = false;
        } catch (NoSuchMethodException ignored) {
            this.destroyPacketConstructor = destroyPacketClass.getConstructor(int.class);
            this.destroyPacketConstructorOneInt = true;
        }

        // 1.17+ :: New way of retrieving player connection instance
        if (ServerVersion.isNewerThan(ServerVersion.V1_19_R3)) {
            this.playerConnectionField = entityPlayerClass.getField("c");
        } else if (ServerVersion.isNewerThan(ServerVersion.V1_16)) {
            this.playerConnectionField = entityPlayerClass.getField("b");
        } else {
            this.playerConnectionField = entityPlayerClass.getField("playerConnection");
        }

        // 1.13+ :: text to display needed to be converted to a IChatBaseComponent component
        if (ServerVersion.isNewerThan(ServerVersion.V1_12)) {
            this.chatMessageClass = getCraftbukkitClass("util.CraftChatMessage");
            this.chatBaseComponentClass = getNMSClass("IChatBaseComponent", "network.chat");
        } else {
            this.chatMessageClass = null;
            this.chatBaseComponentClass = null;
        }

        // 1.14+ :: the living entity constructor must be called with the entity type
        if (ServerVersion.isNewerThan(ServerVersion.V1_13)) {
            Class<?> entityTypesClass = getNMSClass("EntityTypes", "world.entity");
            this.entityTypesInvokationMethod = entityTypesClass.getMethod("a", String.class);
            this.armorStandConstructor = armorStandClass.getConstructor(entityTypesClass, worldClass);
        } else {
            this.entityTypesInvokationMethod = null;
            this.armorStandConstructor = armorStandClass.getConstructor(worldClass);
        }

        // 1.15+ :: entity metadatas have to be sent in separate packet
        if (ServerVersion.isNewerThan(ServerVersion.V1_14)) {
            Class<?> packetMetadataClass = getNMSClass("PacketPlayOutEntityMetadata", "network.protocol.game");
            Class<?> dataWatcherClass = getNMSClass("DataWatcher", "network.syncher");

            // 1.19.3+ :: metadata packet use a list of datawatcher items
            if (ServerVersion.isNewerThan(ServerVersion.V1_19)) {
                this.metadataPacketConstructor = packetMetadataClass.getConstructor(int.class, List.class);
            } else {
                this.metadataPacketConstructor = packetMetadataClass.getConstructor(int.class, dataWatcherClass, boolean.class);
            }
        } else {
            this.metadataPacketConstructor = null;
        }
    }

    /**
     * Retrieves or creates an instance of the utility class.
     *
     * @return utility class instance
     * @throws ReflectiveOperationException thrown if cannot instanciate NMS objects
     */
    public static NMSHologramUtil get() throws ReflectiveOperationException {
        if (instance == null) {
            instance = new NMSHologramUtil();
        }
        return instance;
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
    public int spawnHologram(Location location, String text, Player observer) throws ReflectiveOperationException {
        // Then, we need to generate the fake armorstand
        Object entity = this.createHologramEntity(location.getWorld(), location.getX(), location.getY(), location.getZ(), text);

        // 1.19.3+ — 1.18+ :: New method name in Entity class
        Method getId;
        if (ServerVersion.isNewerThan(ServerVersion.V1_19)) {
            getId = this.entityClass.getMethod(ServerVersion.isNewerThan(ServerVersion.V1_19_R2) ? "af" : "ah");
        } else {
            getId = getNMSDynamicMethod(this.entityClass, "getId", "ae");
        }
        int entityId = (int) getId.invoke(entity);

        // Send the spawn packet for 1.8+
        this.sendPacket(observer, spawnPacketConstructor.newInstance(entity));

        // Send the metadata packet for 1.15+
        if (ServerVersion.isNewerThan(ServerVersion.V1_14)) {
            Object metadataPacket = this.createEntityMetadataPacket(entityId, entity);
            this.sendPacket(observer, metadataPacket);
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
    public void destroyEntity(int entityId, Player observer) throws ReflectiveOperationException {
        Object packet;

        if (this.destroyPacketConstructorOneInt) {
            packet = this.destroyPacketConstructor.newInstance(entityId);
        } else {
            packet = this.destroyPacketConstructor.newInstance(new int[]{entityId});
        }

        this.sendPacket(observer, packet);
    }

    /**
     * Send a packet to the player linked to the hologram.
     *
     * @param player the player who has to receive the packet
     * @param packet the packet to send
     * @throws ReflectiveOperationException error with reflection
     */
    private void sendPacket(Player player, Object packet) throws ReflectiveOperationException {
        Method getHandle = player.getClass().getMethod("getHandle");
        Object entityPlayer = getHandle.invoke(player);
        Object pConnection = this.playerConnectionField.get(entityPlayer);
        Method sendMethod = getNMSDynamicMethod(pConnection.getClass(), "sendPacket", "a", this.packetClass);

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
    private Object createHologramEntity(World w, double x, double y, double z, String text) throws ReflectiveOperationException {
        Object craftWorldObj = this.craftWorldClass.cast(w);
        Method getHandleMethod = craftWorldObj.getClass().getMethod("getHandle");
        Object entityObject;

        // In 1.14+ versions, we need to specify a type to create an instance of an ArmorStand.
        if (ServerVersion.isNewerThan(ServerVersion.V1_13)) {
            Optional<Object> armorStandType = (Optional<Object>) this.entityTypesInvokationMethod.invoke(null, "armor_stand");
            entityObject = this.armorStandConstructor.newInstance(
                    armorStandType.orElseThrow(() -> new NullPointerException("ArmorStand entity type not found")),
                    getHandleMethod.invoke(craftWorldObj));
        } else {
            entityObject = this.armorStandConstructor.newInstance(getHandleMethod.invoke(craftWorldObj));
        }

        // In 1.13+ versions, the custom name must be a IChatBaseComponent component
        if (ServerVersion.isNewerThan(ServerVersion.V1_12)) {
            Method fromStringOrNullMethod = this.chatMessageClass.getMethod("fromStringOrNull", String.class);
            Object chatComponent = fromStringOrNullMethod.invoke(null, text);

            // 1.19+ :: method is now "b" (instead of "a")
            String methodNamePost17 = ServerVersion.isNewerThan(ServerVersion.V1_18) ? "b" : "a";
            getNMSDynamicMethod(entityObject.getClass(), "setCustomName", methodNamePost17, this.chatBaseComponentClass)
                    .invoke(entityObject, chatComponent);
        } else {
            Method setCustomName = entityObject.getClass().getMethod("setCustomName", String.class);
            setCustomName.invoke(entityObject, text);
        }

        // In 1.10+ versions, setGravity has been replaced with setNoGravity
        if (ServerVersion.isNewerThan(ServerVersion.V1_9)) {
            Method setNoGravity = getNMSDynamicMethod(entityObject.getClass(), "setNoGravity", "e", boolean.class);
            setNoGravity.invoke(entityObject, true);
        } else {
            Method setGravity = entityObject.getClass().getMethod("setGravity", boolean.class);
            setGravity.invoke(entityObject, false);
        }

        Method setCustomNameVisible = getNMSDynamicMethod(entityClass, "setCustomNameVisible", "n", boolean.class);
        setCustomNameVisible.invoke(entityObject, true);

        Method setLocation = getNMSDynamicMethod(
                entityObject.getClass(), "setLocation", "a",
                double.class, double.class, double.class, float.class, float.class
        );
        setLocation.invoke(entityObject, x, y, z, 0.0F, 0.0F);

        Method setInvisible = getNMSDynamicMethod(entityObject.getClass(), "setInvisible", "j", boolean.class);
        setInvisible.invoke(entityObject, true);

        return entityObject;
    }

    /**
     * Creates a packet to send metadata of the hologram entity (in 1.15+ versions).
     *
     * @param entityId id of the entity
     * @param entity   entity object
     * @return created packet
     * @throws ReflectiveOperationException if the packet cannot be instanciated
     */
    private Object createEntityMetadataPacket(int entityId, Object entity) throws ReflectiveOperationException {
        Method getDataWatcher;
        if (ServerVersion.isNewerThan(ServerVersion.V1_19)) {
            getDataWatcher = this.entityClass.getMethod(ServerVersion.isNewerThan(ServerVersion.V1_19_R2) ? "aj" : "al");
        } else {
            getDataWatcher = getNMSDynamicMethod(this.entityClass, "getDataWatcher", "ai");
        }
        Object entityDataWatcher = getDataWatcher.invoke(entity);
        Object metadataPacket;

        // 1.19.3+ :: metadata packet use a list of datawatcher items
        if (ServerVersion.isNewerThan(ServerVersion.V1_19)) {
            Method getItemListMethod = entityDataWatcher.getClass().getMethod("b");
            metadataPacket = this.metadataPacketConstructor.newInstance(entityId, getItemListMethod.invoke(entityDataWatcher));
        } else {
            // 1.18+ :: New method name in Entity class
            metadataPacket = this.metadataPacketConstructor.newInstance(entityId, entityDataWatcher, false);
        }

        return metadataPacket;
    }

}

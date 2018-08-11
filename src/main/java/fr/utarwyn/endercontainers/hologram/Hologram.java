package fr.utarwyn.endercontainers.hologram;

import fr.utarwyn.endercontainers.compatibility.ServerVersion;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * This class is used to display a text above an enderchest block
 * if the option blockNametag was set to true.
 * This class uses packets and it's compatible from 1.8.X to 1.12.X.
 * (Tested and approved)
 *
 * @since 2.0.0
 * @author Utarwyn
 */
class Hologram {

	/**
	 * Static field used to calculate the offset between each line
	 * in an hologram.
	 */
	private static final double ABS = 0.23D;

	/**
	 * Reflection NMS Entity class
	 */
	private static Class<?> nmsEntity;

	/**
	 * Reflection NMS Packet class
	 */
	private static Class<?> nmsPacket;

	/**
	 * Reflection CraftWorld class
	 */
	private static Class<?> craftWorld;

	/**
	 * Reflection Packet class
	 */
	private static Class<?> packetClass;

	/**
	 * Reflection EntityLiving class
	 */
	private static Class<?> entityLivingClass;

	/**
	 * CraftChatMessages class for 1.13 holograms
	 */
	private static Class<?> chatMessageClass;

	/**
	 * IChatBaseComponent class for 1.13 holograms
	 */
	private static Class<?> chatBaseComponentClass;

	/**
	 * Constructor of the ArmorStand class
	 */
	private static Constructor<?> armorStandConstructor;

	/**
	 * Constructor for the destroy packet
	 */
	private static Constructor<?> destroyPacketConstructor;

	/**
	 * The title of the hologram (its content)
	 */
	private String title;

	/**
	 * The location where the hologram have to spawn
	 */
	private Location location;

	/**
	 * The player who has to receive the hologram
	 */
	private Player player;

	/**
	 * The destroy packet object generated at the creation of the hologram
	 */
	private Object destroyPacket;

	static {
		String version = ServerVersion.getBukkitVersion();

		try {
			Class<?> worldClass = Class.forName("net.minecraft.server." + version + ".World");
			Class<?> armorStandClass = Class.forName("net.minecraft.server." + version + ".EntityArmorStand");
			Class<?> destroyPacketClass = Class.forName("net.minecraft.server." + version + ".PacketPlayOutEntityDestroy");

			nmsEntity = Class.forName("net.minecraft.server." + version + ".Entity");
			craftWorld = Class.forName("org.bukkit.craftbukkit." + version + ".CraftWorld");
			packetClass = Class.forName("net.minecraft.server." + version + ".PacketPlayOutSpawnEntityLiving");
			entityLivingClass = Class.forName("net.minecraft.server." + version + ".EntityLiving");
			armorStandConstructor = armorStandClass.getConstructor(worldClass);
			destroyPacketConstructor = destroyPacketClass.getConstructor(int[].class);

			// Two classes used to format messages for holograms (only with the 1.13 version)
			if (ServerVersion.is(ServerVersion.V1_13)) {
				chatMessageClass = Class.forName("org.bukkit.craftbukkit." + version + ".util.CraftChatMessage");
				chatBaseComponentClass = Class.forName("net.minecraft.server." + version + ".IChatBaseComponent");
			}

			nmsPacket = Class.forName("net.minecraft.server." + version + ".Packet");
		} catch (ClassNotFoundException | NoSuchMethodException | SecurityException ex) {
			System.err.println("Error - Hologram classes not initialized!");
			ex.printStackTrace();
		}
	}

	/**
	 * Construct an hologram and spawn it directly
	 * @param player The player who has to receive the hologram
	 * @param title The title/content of the hologram
	 * @param location The location of the hologram
	 */
	Hologram(Player player, String title, Location location) {
		this.player = player;
		this.title = title;
		this.location = location;

		this.spawn();
	}

	/**
	 * Know if the player linked to the hologram is online
	 * @return True if the player is online
	 */
	public boolean isPlayerOnline() {
		return this.player != null && this.player.isOnline();
	}

	/**
	 * Destroy the hologram
	 * (Send the destroy packet to the player)
	 */
	void destroy() {
		this.sendPacket(this.destroyPacket);
	}

	/**
	 * Spawn the hologram
	 */
	private void spawn() {
		Location displayLoc = this.location.clone().add(.5, ABS - 1.25D, .5);

		Object packet = this.getPacket(displayLoc.getWorld(), displayLoc.getX(), displayLoc.getY(), displayLoc.getZ(), this.title);

		try {
			Field field = packetClass.getDeclaredField("a");
			field.setAccessible(true);

			this.destroyPacket = this.getDestroyPacket((int) field.get(packet));
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		this.sendPacket(packet);
	}

	/**
	 * Returns the packet used to destroy the hologram
	 * @param id The id of the entity to destroy
	 * @return The destroy packet object
	 */
	private Object getDestroyPacket(int... id) {
		try {
			return destroyPacketConstructor.newInstance((Object) id);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Returns the packet used to spawn the hologram
	 * @param w World
	 * @param x X coordinate
	 * @param y Y coordinate
	 * @param z Z coordinate
	 * @param text Text to display
	 * @return The spawn packet object
	 */
	private Object getPacket(World w, double x, double y, double z, String text) {
		try {
			Object craftWorldObj = craftWorld.cast(w);

			Method getHandleMethod = craftWorldObj.getClass().getMethod("getHandle");
			Object entityObject = armorStandConstructor.newInstance(getHandleMethod.invoke(craftWorldObj));

			if (ServerVersion.is(ServerVersion.V1_13)) {
				Method fromStringOrNullMethod = chatMessageClass.getMethod("fromStringOrNull", String.class);
				Object chatComponent = fromStringOrNullMethod.invoke(null, text);

				Method setCustomName = entityObject.getClass().getMethod("setCustomName", chatBaseComponentClass);
				setCustomName.invoke(entityObject, chatComponent);
			} else {
				Method setCustomName = entityObject.getClass().getMethod("setCustomName", String.class);
				setCustomName.invoke(entityObject, text);
			}

			Method setCustomNameVisible = nmsEntity.getMethod("setCustomNameVisible", boolean.class);
			setCustomNameVisible.invoke(entityObject, true);

			if (ServerVersion.isOlderThan(ServerVersion.V1_10)) { // 1.8 / 1.9
				Method setGravity = entityObject.getClass().getMethod("setGravity", boolean.class);
				setGravity.invoke(entityObject, false);
			} else {                          // 1.10+
				Method setNoGravity = entityObject.getClass().getMethod("setNoGravity", boolean.class);
				setNoGravity.invoke(entityObject, true);
			}

			Method setLocation = entityObject.getClass().getMethod("setLocation", double.class, double.class, double.class, float.class, float.class);
			setLocation.invoke(entityObject, x, y, z, 0.0F, 0.0F);

			Method setInvisible = entityObject.getClass().getMethod("setInvisible", boolean.class);
			setInvisible.invoke(entityObject, true);

			Constructor<?> cw = packetClass.getConstructor(entityLivingClass);
			return cw.newInstance(entityObject);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Send a packet to the player linked to the hologram
	 * @param packet The packet to send
	 */
	private void sendPacket(Object packet) {
		try {
			Method getHandle = this.player.getClass().getMethod("getHandle");
			Object entityPlayer = getHandle.invoke(this.player);
			Object pConnection = entityPlayer.getClass().getField("playerConnection").get(entityPlayer);
			Method sendMethod = pConnection.getClass().getMethod("sendPacket", nmsPacket);

			sendMethod.invoke(pConnection, packet);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}

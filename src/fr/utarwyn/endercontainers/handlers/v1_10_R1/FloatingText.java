package fr.utarwyn.endercontainers.handlers.v1_10_R1;


import fr.utarwyn.endercontainers.utils.FloatingTextUtils;
import net.minecraft.server.v1_10_R1.EntityArmorStand;
import net.minecraft.server.v1_10_R1.EntityPlayer;
import net.minecraft.server.v1_10_R1.PacketPlayOutEntityDestroy;
import net.minecraft.server.v1_10_R1.PacketPlayOutSpawnEntityLiving;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_10_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_10_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.UUID;

public class FloatingText implements FloatingTextUtils.FloatingText{

    private Location location;
    private String text;
    private Player player;

    private EntityArmorStand armorStand;
    private HashMap<UUID, Integer> armorStands = new HashMap<>();

    public FloatingText(Location location, String text) {
        new FloatingText(location, text, null);
    }

    public FloatingText(Location location, String text, Player p) {
        this.location = location;
        this.text = text;
        this.player = p;

        this.spawn();
    }


    public String getText() {
        return this.text;
    }


    public void spawn() {
        armorStand = new EntityArmorStand(((CraftWorld) location.getWorld()).getHandle());
        armorStand.setLocation(location.getX(), location.getY() - 1.8, location.getZ(), location.getYaw(), location.getPitch());

        armorStand.setCustomName(this.text);
        armorStand.setCustomNameVisible(true);

        armorStand.setNoGravity(true);
        armorStand.setInvisible(true);
        armorStand.setSmall(true);
        armorStand.setBasePlate(false);

        if (this.player == null) {
            PacketPlayOutSpawnEntityLiving packet = new PacketPlayOutSpawnEntityLiving(this.armorStand);

            for (Player p : Bukkit.getOnlinePlayers()) {
                EntityPlayer nmsPlayer = ((CraftPlayer) p).getHandle();
                nmsPlayer.playerConnection.sendPacket(packet);

                armorStands.put(p.getUniqueId(), armorStand.getId());
            }
        } else spawnFor(this.player);
    }

    public void spawnFor(Player p) {
        if (this.armorStand == null) return;

        PacketPlayOutSpawnEntityLiving packet = new PacketPlayOutSpawnEntityLiving(this.armorStand);

        EntityPlayer nmsPlayer = ((CraftPlayer) p).getHandle();
        nmsPlayer.playerConnection.sendPacket(packet);

        armorStands.put(p.getUniqueId(), armorStand.getId());
    }

    public void remove() {
        for (UUID uuid : armorStands.keySet()) {
            PacketPlayOutEntityDestroy packet = new PacketPlayOutEntityDestroy(armorStands.get(uuid));
            EntityPlayer nmsPlayer = ((CraftPlayer) Bukkit.getPlayer(uuid)).getHandle();
            nmsPlayer.playerConnection.sendPacket(packet);
        }
    }

}
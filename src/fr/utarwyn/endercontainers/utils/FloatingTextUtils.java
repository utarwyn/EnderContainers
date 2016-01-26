package fr.utarwyn.endercontainers.utils;

import net.minecraft.server.v1_8_R3.EntityArmorStand;
import net.minecraft.server.v1_8_R3.EntityPlayer;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityDestroy;
import net.minecraft.server.v1_8_R3.PacketPlayOutSpawnEntityLiving;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class FloatingTextUtils {

    private static ArrayList<FloatingText> floatingTexts = new ArrayList<>();


    public static ArrayList<FloatingText> getFloatingTexts() {
        return floatingTexts;
    }


    public static FloatingText displayFloatingTextAt(String text, Location loc) {
        FloatingText floatingText = new FloatingText(loc, text);

        floatingTexts.add(floatingText);
        return floatingText;
    }

    public static FloatingText displayFloatingTextAt(String text, World world, float x, float y, float z) {
        return displayFloatingTextAt(text, new Location(world, x, y, z));
    }

    public static FloatingText displayFloatingTextAtFor(String text, Location loc, Player player) {
        FloatingText floatingText = new FloatingText(loc, text, player);

        floatingTexts.add(floatingText);
        return floatingText;
    }


    public static void removeFloatingText(FloatingText floatingText) {
        if (floatingTexts.contains(floatingText)) {
            try {
                floatingText.remove();
            } catch (Exception e) {
                e.printStackTrace();
            }
            floatingTexts.remove(floatingText);
        }
    }

    public static void removeFloatingText(String text) {
        for (FloatingText floatingText : floatingTexts) {
            if (!floatingText.getText().equalsIgnoreCase(text)) continue;

            try {
                floatingText.remove();
            } catch (Exception e) {
                e.printStackTrace();
            }

            floatingTexts.remove(floatingText);
        }
    }

    public static void clearFloatingTexts() {
        for (FloatingText floatingText : floatingTexts) {
            try {
                floatingText.remove();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        floatingTexts.clear();
    }


    public static class FloatingText {

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
            armorStand.setLocation(location.getX(), location.getY() - 1, location.getZ(), location.getYaw(), location.getPitch());

            armorStand.setCustomName(this.text);
            armorStand.setCustomNameVisible(true);

            armorStand.setGravity(false);
            armorStand.setInvisible(true);
            armorStand.setSmall(true);

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


}

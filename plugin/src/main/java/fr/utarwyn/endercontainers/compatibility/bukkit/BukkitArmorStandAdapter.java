package fr.utarwyn.endercontainers.compatibility.bukkit;

import fr.utarwyn.endercontainers.compatibility.ArmorStandAdapter;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.Objects;

public class BukkitArmorStandAdapter implements ArmorStandAdapter {

    @Override
    public int spawnArmorStandFor(Plugin plugin, Player observer, Location location, String text) {
        ArmorStand armorStand = (ArmorStand) Objects.requireNonNull(location.getWorld()).spawnEntity(location.add(0, 2, 0), EntityType.ARMOR_STAND);

        armorStand.setInvisible(true);
        armorStand.setCustomName(text);
        armorStand.setCustomNameVisible(true);
        armorStand.setGravity(false);
        armorStand.setInvulnerable(true);
        armorStand.setMarker(true);

        observer.showEntity(plugin, armorStand);

        return armorStand.getEntityId();
    }

    @Override
    public void destroyArmorStandFor(Player observer, int entityId) {
        observer.getWorld().getEntities().stream()
                .filter(e -> e.getEntityId() == entityId)
                .findFirst()
                .ifPresent(Entity::remove);

    }

}

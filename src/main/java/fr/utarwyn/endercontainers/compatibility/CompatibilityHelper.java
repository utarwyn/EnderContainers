package fr.utarwyn.endercontainers.compatibility;

import com.google.common.base.Preconditions;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static fr.utarwyn.endercontainers.compatibility.ServerVersion.V1_12;

/**
 * Helper class to solve compatibility problems.
 *
 * @author Utarwyn
 * @since 2.2.0
 */
public class CompatibilityHelper {

    private static final Map<Integer, Material> MATERIAL_BY_IDS;

    private static final Map<Integer, String> ENCHANT_BY_IDS;

    static {
        MATERIAL_BY_IDS = new ConcurrentHashMap<>();
        ENCHANT_BY_IDS = new HashMap<>();

        ENCHANT_BY_IDS.put(0, "PROTECTION_ENVIRONMENTAL");
        ENCHANT_BY_IDS.put(1, "PROTECTION_FIRE");
        ENCHANT_BY_IDS.put(2, "PROTECTION_FALL");
        ENCHANT_BY_IDS.put(3, "PROTECTION_EXPLOSIONS");
        ENCHANT_BY_IDS.put(4, "PROTECTION_PROJECTILE");
        ENCHANT_BY_IDS.put(5, "OXYGEN");
        ENCHANT_BY_IDS.put(6, "WATER_WORKER");
        ENCHANT_BY_IDS.put(7, "THORNS");
        ENCHANT_BY_IDS.put(8, "DEPTH_STRIDER");
        ENCHANT_BY_IDS.put(9, "FROST_WALKER");
        ENCHANT_BY_IDS.put(10, "BINDING_CURSE");
        ENCHANT_BY_IDS.put(16, "DAMAGE_ALL");
        ENCHANT_BY_IDS.put(17, "DAMAGE_UNDEAD");
        ENCHANT_BY_IDS.put(18, "DAMAGE_ARTHROPODS");
        ENCHANT_BY_IDS.put(19, "KNOCKBACK");
        ENCHANT_BY_IDS.put(20, "FIRE_ASPECT");
        ENCHANT_BY_IDS.put(21, "LOOT_BONUS_MOBS");
        ENCHANT_BY_IDS.put(22, "SWEEPING_EDGE");
        ENCHANT_BY_IDS.put(32, "DIG_SPEED");
        ENCHANT_BY_IDS.put(33, "SILK_TOUCH");
        ENCHANT_BY_IDS.put(34, "DURABILITY");
        ENCHANT_BY_IDS.put(35, "LOOT_BONUS_BLOCKS");
        ENCHANT_BY_IDS.put(48, "ARROW_DAMAGE");
        ENCHANT_BY_IDS.put(49, "ARROW_KNOCKBACK");
        ENCHANT_BY_IDS.put(50, "ARROW_FIRE");
        ENCHANT_BY_IDS.put(51, "ARROW_INFINITE");
        ENCHANT_BY_IDS.put(61, "LUCK");
        ENCHANT_BY_IDS.put(62, "LURE");
        ENCHANT_BY_IDS.put(65, "LOYALTY");
        ENCHANT_BY_IDS.put(66, "IMPALING");
        ENCHANT_BY_IDS.put(67, "RIPTIDE");
        ENCHANT_BY_IDS.put(68, "CHANNELING");
        ENCHANT_BY_IDS.put(70, "MENDING");
        ENCHANT_BY_IDS.put(71, "VANISHING_CURSE");
    }

    private CompatibilityHelper() {

    }

    public static Material matchMaterial(String name) {
        Preconditions.checkNotNull(name, "Material name cannot be null!");

        Material material = Material.matchMaterial(name);

        if (material == null && ServerVersion.isNewerThan(V1_12)) {
            material = Material.matchMaterial(name, true);
        }

        return material;
    }

    public static Material materialFromId(int id) {
        Material foundMaterial = null;

        // Is cache already contains the material?
        if (MATERIAL_BY_IDS.containsKey(id)) {
            return MATERIAL_BY_IDS.get(id);
        }

        if (ServerVersion.isOlderThan(ServerVersion.V1_13)) {
            // We have to use Java reflection here because the method does not
            // exist in 1.13+ versions of the Bukkit API.
            try {
                Method getMethod = Material.class.getMethod("getMaterial", Integer.class);
                foundMaterial = (Material) getMethod.invoke(null, id);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            // We may not use material ids in this versions of Bukkit!
            // So we have to use a tricky method to get the correct material.
            for (Material material : Material.values()) {
                if (material.getId() == id) {
                    foundMaterial = material;
                }
            }
        }

        // Put the found material in the cache.
        if (foundMaterial != null) {
            MATERIAL_BY_IDS.put(id, foundMaterial);
        }

        return foundMaterial;
    }

    public static String enchantmentToString(Enchantment enchantment) {
        if (ServerVersion.isNewerThan(V1_12)) {
            // New method to save en enchantment!
            NamespacedKey key = enchantment.getKey();
            return key.getNamespace() + "!" + key.getKey();
        } else {
            // In old versions we only use the name of the enchantment!
            return enchantment.getName();
        }
    }

    public static Enchantment enchantmentFromString(String value) {
        if (!StringUtils.isNumeric(value)) { // Name or NamespacedKey
            if (ServerVersion.isNewerThan(V1_12) && value.contains("!")) {
                // New method to get en enchantment!
                String[] parts = value.split("!");
                return Enchantment.getByKey(new NamespacedKey(parts[0], parts[1]));
            } else {
                // Legacy method: use the name (old versions of Bukkit)
                return Enchantment.getByName(value);
            }
        } else { // Legacy support for enchant ids
            return Enchantment.getByName(ENCHANT_BY_IDS.get(Integer.valueOf(value)));
        }
    }

}

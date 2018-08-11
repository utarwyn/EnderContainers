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

import static fr.utarwyn.endercontainers.compatibility.ServerVersion.V1_13;

/**
 * Helper class to solve compatibility problems.
 *
 * @since 2.2.0
 * @author Utarwyn
 */
public class CompatibilityHelper {

	private static final Map<Integer, Material> MATERIAL_BY_IDS;

	private static final Map<Integer, String> ENCHANT_BY_IDS;

	private CompatibilityHelper() {

	}

	static {
		MATERIAL_BY_IDS = new ConcurrentHashMap<>();
		ENCHANT_BY_IDS = new HashMap<Integer, String>() {{
			put(0, "PROTECTION_ENVIRONMENTAL");
			put(1, "PROTECTION_FIRE");
			put(2, "PROTECTION_FALL");
			put(3, "PROTECTION_EXPLOSIONS");
			put(4, "PROTECTION_PROJECTILE");
			put(5, "OXYGEN");
			put(6, "WATER_WORKER");
			put(7, "THORNS");
			put(8, "DEPTH_STRIDER");
			put(9, "FROST_WALKER");
			put(10, "BINDING_CURSE");
			put(16, "DAMAGE_ALL");
			put(17, "DAMAGE_UNDEAD");
			put(18, "DAMAGE_ARTHROPODS");
			put(19, "KNOCKBACK");
			put(20, "FIRE_ASPECT");
			put(21, "LOOT_BONUS_MOBS");
			put(22, "SWEEPING_EDGE");
			put(32, "DIG_SPEED");
			put(33, "SILK_TOUCH");
			put(34, "DURABILITY");
			put(35, "LOOT_BONUS_BLOCKS");
			put(48, "ARROW_DAMAGE");
			put(49, "ARROW_KNOCKBACK");
			put(50, "ARROW_FIRE");
			put(51, "ARROW_INFINITE");
			put(61, "LUCK");
			put(62, "LURE");
			put(65, "LOYALTY");
			put(66, "IMPALING");
			put(67, "RIPTIDE");
			put(68, "CHANNELING");
			put(70, "MENDING");
			put(71, "VANISHING_CURSE");
		}};
	}

	public static Material matchMaterial(String name) {
		Preconditions.checkNotNull(name, "Material name cannot be null!");

		Material material = Material.matchMaterial(name);

		if (material == null && ServerVersion.is(V1_13)) {
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

		if (ServerVersion.isOlderThan(V1_13)) {
			// We have to use Java reflection here because the method does not
			// exist in the version 1.13 of the Bukkit API.
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
		if (ServerVersion.is(V1_13)) {
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
			if (ServerVersion.is(V1_13) && value.contains("!")) {
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

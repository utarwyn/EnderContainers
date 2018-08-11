package fr.utarwyn.endercontainers.compatibility;

import com.google.common.base.Preconditions;
import org.bukkit.Material;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static fr.utarwyn.endercontainers.compatibility.ServerVersion.V1_13;

/**
 * Helper class for materials (to solve compatibility problems).
 *
 * @since 2.2.0
 * @author Utarwyn
 */
public class MaterialHelper {

	private static final Map<Integer, Material> BY_IDS;

	private MaterialHelper() {

	}

	static {
		BY_IDS = new ConcurrentHashMap<>();
	}

	public static Material match(String name) {
		Preconditions.checkNotNull(name, "Material name cannot be null!");

		Material material = Material.matchMaterial(name);

		if (material == null && ServerVersion.is(V1_13)) {
			material = Material.matchMaterial(name, true);
		}

		return material;
	}

	public static Material fromId(int id) {
		Material foundMaterial = null;

		// Is cache already contains the material?
		if (BY_IDS.containsKey(id)) {
			return BY_IDS.get(id);
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
				if (material.name().startsWith("LEGACY_") && material.getId() == id) {
					foundMaterial = material;
				}
			}
		}

		// Put the found material in the cache.
		if (foundMaterial != null) {
			BY_IDS.put(id, foundMaterial);
		}

		return foundMaterial;
	}

}

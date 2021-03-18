package fr.utarwyn.endercontainers.compatibility;

import com.google.common.base.Preconditions;
import org.bukkit.Material;

import static fr.utarwyn.endercontainers.compatibility.ServerVersion.V1_12;

/**
 * Helper class to solve compatibility problems.
 *
 * @author Utarwyn
 * @since 2.2.0
 */
public class CompatibilityHelper {

    private CompatibilityHelper() {
        // not implemented
    }

    public static Material matchMaterial(String name) {
        Preconditions.checkNotNull(name, "Material name cannot be null!");

        Material material = Material.matchMaterial(name);

        if (material == null && ServerVersion.isNewerThan(V1_12)) {
            material = Material.matchMaterial(name, true);
        }

        return material;
    }

}

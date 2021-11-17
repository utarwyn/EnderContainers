package fr.utarwyn.endercontainers.compatibility;

import com.google.common.base.Preconditions;
import org.bukkit.Material;
import org.bukkit.Sound;

import java.util.HashMap;
import java.util.Map;

import static fr.utarwyn.endercontainers.compatibility.ServerVersion.V1_12;

/**
 * Helper class to solve compatibility problems.
 *
 * @author Utarwyn
 * @since 2.2.0
 */
public class CompatibilityHelper {

    /**
     * Stores cached sounds
     */
    private static final Map<String, Sound> SOUND_MAP = new HashMap<>();

    private CompatibilityHelper() {
        // not implemented
    }

    /**
     * Searches for a material from its name using internal Bukkit methods.
     *
     * @param name name of the material to match
     * @return found material, null otherwise
     */
    public static Material searchMaterial(String name) {
        Preconditions.checkNotNull(name, "material name must be defined");

        Material material = Material.matchMaterial(name);
        if (material == null && ServerVersion.isNewerThan(V1_12)) {
            material = Material.matchMaterial(name, true);
        }

        return material;
    }

    /**
     * Searches for a Bukkit sound using one of provided names.
     * Caches the sound based on provided names if found.
     *
     * @param names list of names to search
     * @return found sound
     * @throws IllegalArgumentException thrown if no sound has been found
     */
    public static Sound searchSound(String... names) throws IllegalArgumentException {
        return SOUND_MAP.computeIfAbsent(names[0], (key) -> {
            for (String soundKey : names) {
                try {
                    return Sound.valueOf(soundKey);
                } catch (IllegalArgumentException ignored) {
                }
            }
            throw new IllegalArgumentException(String.format("no sound found using key %s", names[0]));
        });
    }

}

package fr.utarwyn.endercontainers.compatibility;

import org.bukkit.Material;

import java.util.HashMap;
import java.util.Map;

/**
 * Selects materials based on the current server version.
 * Provides fallback materials for older versions that don't support newer materials.
 *
 * @author Utarwyn
 * @since 2.3.0
 */
public class VersionAwareMaterialSelector {

    /**
     * Mapping of modern materials to their fallbacks for older versions
     */
    private static final Map<String, String> MATERIAL_FALLBACKS = new HashMap<>();

    static {
        // Stained glass pane color mappings for 1.8.8 (uses damage values)
        MATERIAL_FALLBACKS.put("RED_STAINED_GLASS_PANE", "STAINED_GLASS_PANE:14");
        MATERIAL_FALLBACKS.put("ORANGE_STAINED_GLASS_PANE", "STAINED_GLASS_PANE:1");
        MATERIAL_FALLBACKS.put("YELLOW_STAINED_GLASS_PANE", "STAINED_GLASS_PANE:4");
        MATERIAL_FALLBACKS.put("LIME_STAINED_GLASS_PANE", "STAINED_GLASS_PANE:5");
        MATERIAL_FALLBACKS.put("GREEN_STAINED_GLASS_PANE", "STAINED_GLASS_PANE:13");
        MATERIAL_FALLBACKS.put("CYAN_STAINED_GLASS_PANE", "STAINED_GLASS_PANE:9");
        MATERIAL_FALLBACKS.put("BLUE_STAINED_GLASS_PANE", "STAINED_GLASS_PANE:11");
        MATERIAL_FALLBACKS.put("PURPLE_STAINED_GLASS_PANE", "STAINED_GLASS_PANE:10");
        MATERIAL_FALLBACKS.put("PINK_STAINED_GLASS_PANE", "STAINED_GLASS_PANE:6");
        MATERIAL_FALLBACKS.put("BROWN_STAINED_GLASS_PANE", "STAINED_GLASS_PANE:12");
        MATERIAL_FALLBACKS.put("BLACK_STAINED_GLASS_PANE", "STAINED_GLASS_PANE:15");
        MATERIAL_FALLBACKS.put("WHITE_STAINED_GLASS_PANE", "STAINED_GLASS_PANE:0");
        MATERIAL_FALLBACKS.put("LIGHT_GRAY_STAINED_GLASS_PANE", "STAINED_GLASS_PANE:8");
        MATERIAL_FALLBACKS.put("GRAY_STAINED_GLASS_PANE", "STAINED_GLASS_PANE:7");
        MATERIAL_FALLBACKS.put("LIGHT_BLUE_STAINED_GLASS_PANE", "STAINED_GLASS_PANE:3");
        MATERIAL_FALLBACKS.put("MAGENTA_STAINED_GLASS_PANE", "STAINED_GLASS_PANE:2");
    }

    /**
     * Selects a material based on the material name and current server version.
     * Automatically provides fallback materials for older versions.
     *
     * @param materialName the name of the material to select
     * @return the Material object, or null if no suitable material is found
     */
    public static Material selectMaterial(String materialName) {
        if (materialName == null || materialName.isEmpty()) {
            return null;
        }

        // Try primary material name
        Material material = Material.matchMaterial(materialName);
        if (material != null) {
            return material;
        }

        // If primary material not found and we're on an older version, try fallback
        if (ServerVersion.isOlderThan(ServerVersion.V1_13)) {
            String fallback = MATERIAL_FALLBACKS.get(materialName);
            if (fallback != null) {
                String[] parts = fallback.split(":");
                material = Material.matchMaterial(parts[0]);
                if (material != null) {
                    return material;
                }
            }
        }

        // Final fallback to GLASS_PANE for any version
        material = Material.matchMaterial("GLASS_PANE");
        if (material != null) {
            return material;
        }

        // Last resort - try GLASS
        return Material.matchMaterial("GLASS");
    }

    /**
     * Extracts durability/damage value from material string.
     * Handles both new format (MATERIAL_NAME:durability) and fallback formats.
     *
     * @param materialString the material string with optional durability
     * @return the durability value, or null if not specified
     */
    public static Integer extractDurability(String materialString) {
        if (materialString == null || materialString.isEmpty()) {
            return null;
        }

        try {
            String[] parts = materialString.split(":");

            // If durability is already in the string (MATERIAL:5 format)
            if (parts.length > 1) {
                return Integer.parseInt(parts[1]);
            }

            // If no durability in string but we're on an older version, look up fallback durability
            if (ServerVersion.isOlderThan(ServerVersion.V1_13)) {
                String fallback = MATERIAL_FALLBACKS.get(materialString);
                if (fallback != null && fallback.contains(":")) {
                    String[] fallbackParts = fallback.split(":");
                    if (fallbackParts.length > 1) {
                        return Integer.parseInt(fallbackParts[1]);
                    }
                }
            }

            return null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

}

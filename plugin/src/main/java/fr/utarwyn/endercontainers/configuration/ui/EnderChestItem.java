package fr.utarwyn.endercontainers.configuration.ui;

import fr.utarwyn.endercontainers.compatibility.VersionAwareMaterialSelector;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

import java.util.List;
import java.util.Objects;

public class EnderChestItem {

    private final String name;

    private final String type;

    private final List<String> lore;

    private final Material material;

    private final Integer durability;

    public EnderChestItem(ConfigurationSection section) {
        this(
                Objects.requireNonNull(section, "configuration section cannot be null").getString("name"),
                section.getString("type"),
                section.getStringList("lore")
        );
    }

    public EnderChestItem(String name, String type, List<String> lore) {
        this.name = name;
        this.type = type;
        this.lore = lore;
        if (type != null) {
            this.material = formatMaterial(type);
            this.durability = formatDurability(type);
        } else {
            this.material = null;
            this.durability = null;
        }
    }

    private static Material formatMaterial(String type) {
        Material material = VersionAwareMaterialSelector.selectMaterial(type.split(":")[0]);
        return Objects.requireNonNull(
                material,
                String.format("item material %s is not valid", type)
        );
    }

    private static Integer formatDurability(String type) {
        return VersionAwareMaterialSelector.extractDurability(type);
    }

    public String getName() {
        return this.name;
    }

    public String getType() {
        return this.type;
    }

    public List<String> getLore() {
        return this.lore;
    }

    public Material getMaterial() {
        return this.material;
    }

    public Integer getDurability() {
        return this.durability;
    }

}

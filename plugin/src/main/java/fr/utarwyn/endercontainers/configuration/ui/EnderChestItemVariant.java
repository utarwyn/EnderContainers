package fr.utarwyn.endercontainers.configuration.ui;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class EnderChestItemVariant extends EnderChestItem {

    private final EnderChestItemVariantCondition condition;

    public EnderChestItemVariant(EnderChestItem defaultItem, Map<String, Object> section) {
        super(
                (String) section.getOrDefault("name", defaultItem.getName()),
                (String) section.getOrDefault("type", defaultItem.getType()),
                (List<String>) section.getOrDefault("lore", defaultItem.getLore())
        );
        this.condition = new EnderChestItemVariantCondition(
                Objects.requireNonNull((String) section.get("condition"), "variant must have a condition")
        );
    }

    public EnderChestItemVariantCondition getCondition() {
        return this.condition;
    }

}

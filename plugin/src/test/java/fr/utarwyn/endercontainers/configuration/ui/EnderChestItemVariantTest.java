package fr.utarwyn.endercontainers.configuration.ui;

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;

public class EnderChestItemVariantTest {

    private EnderChestItem defaultItem;

    @Before
    public void setUp() {
        this.defaultItem = new EnderChestItem("default name", "LIME_STAINED_GLASS_PANE", Arrays.asList("line1", "line2"));
    }

    @Test
    public void creationWithAllDefaultItemFields() {
        Map<String, Object> map = new HashMap<String, Object>() {{
            put("condition", "inaccessible");
        }};
        EnderChestItemVariant variant = new EnderChestItemVariant(defaultItem, map);

        assertThat(variant.getName()).isEqualTo("default name");
        assertThat(variant.getType()).isEqualTo("LIME_STAINED_GLASS_PANE");
        assertThat(variant.getLore()).isEqualTo(Arrays.asList("line1", "line2"));
        assertThat(variant.getCondition()).isNotNull();
    }

    @Test
    public void creationWithVariantItemFields() {
        Map<String, Object> map = new HashMap<String, Object>() {{
            put("name", "variant name");
            put("type", "RED_STAINED_GLASS_PANE");
            put("lore", Arrays.asList("variant line 1", "variant line 2"));
            put("condition", "inaccessible");
        }};
        EnderChestItemVariant variant = new EnderChestItemVariant(defaultItem, map);

        assertThat(variant.getName()).isEqualTo("variant name");
        assertThat(variant.getType()).isEqualTo("RED_STAINED_GLASS_PANE");
        assertThat(variant.getLore()).isEqualTo(Arrays.asList("variant line 1", "variant line 2"));
        assertThat(variant.getCondition()).isNotNull();
    }

    @Test
    public void creationErrorWithoutCondition() {
        assertThat(assertThrows(NullPointerException.class, () -> new EnderChestItemVariant(defaultItem, new HashMap<>())).getMessage())
                .isEqualTo("variant must have a condition");
    }

}

package fr.utarwyn.endercontainers.configuration.ui;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EnderChestItemTest {

    @Test
    void createWithConfigurationSection() {
        ConfigurationSection section = mock(ConfigurationSection.class);
        when(section.getString("name")).thenReturn("fake name");
        when(section.getString("type")).thenReturn("LIME_STAINED_GLASS_PANE:30");
        when(section.getStringList("lore")).thenReturn(Arrays.asList("line1", "line2"));

        EnderChestItem item = new EnderChestItem(section);
        assertThat(item.getName()).isEqualTo("fake name");
        assertThat(item.getType()).isEqualTo("LIME_STAINED_GLASS_PANE:30");
        assertThat(item.getMaterial()).isEqualTo(Material.LIME_STAINED_GLASS_PANE);
        assertThat(item.getDurability()).isEqualTo(30);
        assertThat(item.getLore()).isEqualTo(Arrays.asList("line1", "line2"));
    }

    @Test
    void handleNullType() {
        EnderChestItem item = new EnderChestItem(null, null, Collections.emptyList());
        assertThat(item.getType()).isNull();
        assertThat(item.getMaterial()).isNull();
        assertThat(item.getDurability()).isNull();
    }

    @Test
    void handleTypeWithoutDurability() {
        EnderChestItem item = new EnderChestItem(null, "LIME_STAINED_GLASS_PANE", Collections.emptyList());
        assertThat(item.getType()).isEqualTo("LIME_STAINED_GLASS_PANE");
        assertThat(item.getMaterial()).isEqualTo(Material.LIME_STAINED_GLASS_PANE);
        assertThat(item.getDurability()).isNull();
    }

    @Test
    void creationErrorWithNullConfigSection() {
        Throwable exception = assertThrows(NullPointerException.class, () -> new EnderChestItem(null));
        assertThat(exception.getMessage()).isEqualTo("configuration section cannot be null");
    }

    @Test
    void creationErrorWithUnknownMaterial() {
        List<String> lore = Collections.emptyList();
        Throwable exception = assertThrows(NullPointerException.class, () -> new EnderChestItem(null, "FAKE_MATERIAL", lore));
        assertThat(exception.getMessage()).isEqualTo("item material FAKE_MATERIAL is not valid");
    }

}

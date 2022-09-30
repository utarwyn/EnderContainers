package fr.utarwyn.endercontainers.configuration.ui;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class EnderChestItemTest {

    @Test
    public void createWithConfigurationSection() {
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
    public void handleNullType() {
        EnderChestItem item = new EnderChestItem(null, null, Collections.emptyList());
        assertThat(item.getType()).isNull();
        assertThat(item.getMaterial()).isNull();
        assertThat(item.getDurability()).isNull();
    }

    @Test
    public void handleTypeWithoutDurability() {
        EnderChestItem item = new EnderChestItem(null, "LIME_STAINED_GLASS_PANE", Collections.emptyList());
        assertThat(item.getType()).isEqualTo("LIME_STAINED_GLASS_PANE");
        assertThat(item.getMaterial()).isEqualTo(Material.LIME_STAINED_GLASS_PANE);
        assertThat(item.getDurability()).isNull();
    }

    @Test
    public void creationErrorWithNullConfigSection() {
        assertThat(assertThrows(NullPointerException.class, () -> new EnderChestItem(null)).getMessage())
                .isEqualTo("configuration section cannot be null");
    }

    @Test
    public void creationErrorWithUnknownMaterial() {
        assertThat(assertThrows(NullPointerException.class, () -> new EnderChestItem(null, "FAKE_MATERIAL", Collections.emptyList())).getMessage())
                .isEqualTo("item material FAKE_MATERIAL is not valid");
    }

}

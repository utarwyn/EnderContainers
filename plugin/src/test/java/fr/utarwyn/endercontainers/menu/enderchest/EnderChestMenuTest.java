package fr.utarwyn.endercontainers.menu.enderchest;

import fr.utarwyn.endercontainers.TestHelper;
import fr.utarwyn.endercontainers.configuration.wrapper.YamlFileLoadException;
import fr.utarwyn.endercontainers.enderchest.EnderChest;
import org.bukkit.Material;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.inventory.ItemStack;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class EnderChestMenuTest {

    private EnderChestMenu menu;

    @Mock
    private EnderChest chest;

    @Before
    public void setUp() throws ReflectiveOperationException, YamlFileLoadException,
            InvalidConfigurationException, IOException {
        TestHelper.setUpFiles();

        UUID uuid = TestHelper.getPlayer().getUniqueId();
        when(this.chest.getOwner()).thenReturn(uuid);
        when(this.chest.getRows()).thenReturn(3);
        when(this.chest.getContents()).thenReturn(new ConcurrentHashMap<>());

        this.menu = new EnderChestMenu(chest);
    }

    @Test
    public void mapContents() {
        // Create fake items for the testing menu
        ItemStack[] itemList = new ItemStack[10];
        itemList[2] = new ItemStack(Material.ENDER_CHEST);
        itemList[8] = new ItemStack(Material.IRON_BLOCK, 2);
        itemList[9] = new ItemStack(Material.GRASS, 20);
        when(this.menu.getInventory().getContents()).thenReturn(itemList);

        // Reload items of the chest
        when(this.chest.getContents()).thenReturn(new ConcurrentHashMap<Integer, ItemStack>() {{
            put(1, new ItemStack(Material.DIAMOND, 3));
            // This item should be replaced by the one in the fake container:
            put(9, new ItemStack(Material.GOLDEN_AXE, 1));
            put(35, new ItemStack(Material.SPRUCE_FENCE, 16));
        }});
        this.menu.prepare();

        Map<Integer, ItemStack> map = this.menu.getMapContents();

        assertThat(map).isNotNull().hasSize(5);
        assertThat(map.get(0)).isNull();
        assertThat(map.get(1)).isNotNull();
        assertThat(map.get(2)).isNotNull();
        assertThat(map.get(8)).isNotNull();
        assertThat(map.get(9)).isNotNull();
        assertThat(map.get(9).getType()).isEqualTo(Material.GRASS);
        assertThat(map.get(9).getAmount()).isEqualTo(20);
        assertThat(map.get(35)).isNotNull();
    }

}

package fr.utarwyn.endercontainers.menu.enderchest;

import fr.utarwyn.endercontainers.TestHelper;
import fr.utarwyn.endercontainers.configuration.wrapper.YamlFileLoadException;
import fr.utarwyn.endercontainers.enderchest.EnderChest;
import fr.utarwyn.endercontainers.enderchest.EnderChestManager;
import org.assertj.core.api.Condition;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

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
        when(this.chest.getMaxSize()).thenReturn(27);
        when(this.chest.getContents()).thenReturn(new ConcurrentHashMap<>());

        this.menu = new EnderChestMenu(chest);
    }

    @Test
    public void prepare() {
        // some fake items stored in the chest
        ItemStack[] containerItems = new ItemStack[27];
        ItemStack item1 = new ItemStack(Material.JUKEBOX, 2);
        ItemStack item2 = new ItemStack(Material.BONE_MEAL, 17);
        ItemStack item3 = new ItemStack(Material.GOLD_INGOT, 25);

        when(this.chest.getContents()).thenReturn(new ConcurrentHashMap<Integer, ItemStack>() {{
            put(4, item1);
            put(7, item2);
            put(38, item3);
        }});

        // Mock both inventory methods
        when(this.menu.getInventory().getContents())
                .thenReturn(containerItems);
        doAnswer(answer -> containerItems[answer.getArgument(0, Integer.class)] = answer.getArgument(1, ItemStack.class))
                .when(this.menu.getInventory()).setItem(anyInt(), any());

        // inventory and internal map empty
        assertThat(this.menu.getContents()).isEmpty();
        assertThat(this.menu.getInventory().getContents()).containsOnlyNulls();

        // Call the real method here!
        this.menu.prepare();

        // all items that are in bounds must be added to the container, but all in the internal map
        assertThat(this.menu.getContents())
                .isNotEmpty().hasSize(3)
                .contains(entry(4, item1), entry(7, item2), entry(38, item3));

        assertThat(this.menu.getInventory().getContents())
                .areExactly(2, new Condition<>(Objects::nonNull, "non null item"))
                .contains(item1, item2)
                .doesNotContain(item3);
    }

    @Test
    public void updateContentsFromContainer() {
        // Create fake items for the testing container
        ItemStack[] itemList = new ItemStack[27];
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
        this.menu.updateContentsFromContainer();

        Map<Integer, ItemStack> map = this.menu.getContents();

        assertThat(map).isNotNull().hasSize(4);
        assertThat(map.get(0)).isNull(); // no item here
        assertThat(map.get(1)).isNull(); // item removed from the container
        assertThat(map.get(2)).isNotNull(); // still present in the container
        assertThat(map.get(8)).isNotNull(); // still present in the container
        assertThat(map.get(9)).isNotNull(); // still present in the container
        assertThat(map.get(9).getType()).isEqualTo(Material.GRASS);
        assertThat(map.get(9).getAmount()).isEqualTo(20);
        assertThat(map.get(35)).isNotNull(); // not in the container but out of bounds, so OK
    }

    @Test
    public void saveOnClose() throws ReflectiveOperationException {
        EnderChestManager manager = mock(EnderChestManager.class);
        Player viewer = mock(Player.class);
        Location location = mock(Location.class);

        when(location.getWorld()).thenReturn(mock(World.class));
        when(viewer.getLocation()).thenReturn(location);
        TestHelper.registerManagers(manager);

        // do not save if the owner is connected
        this.menu.onClose(viewer);
        verify(manager, never()).savePlayerContext(any(), eq(true));

        // save if owner not connected
        UUID offline = UUID.fromString("62dcb385-f2ac-472f-9d88-a0cc0d957082");
        when(this.chest.getOwner()).thenReturn(offline);
        this.menu.onClose(viewer);
        verify(manager).savePlayerContext(this.chest.getOwner(), true);
    }

    @Test
    public void soundOnClose() {
        Player viewer = mock(Player.class);
        Location location = mock(Location.class);
        World world = mock(World.class);

        when(location.getWorld()).thenReturn(world);
        when(viewer.getLocation()).thenReturn(location);

        this.menu.onClose(viewer);
        verify(world).playSound(location, Sound.BLOCK_CHEST_CLOSE, 1f, 1f);
    }

}

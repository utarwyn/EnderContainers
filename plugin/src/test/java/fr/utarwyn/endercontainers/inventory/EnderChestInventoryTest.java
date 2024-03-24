package fr.utarwyn.endercontainers.inventory;

import fr.utarwyn.endercontainers.TestHelper;
import fr.utarwyn.endercontainers.TestInitializationException;
import fr.utarwyn.endercontainers.configuration.enderchests.SaveMode;
import fr.utarwyn.endercontainers.enderchest.EnderChest;
import fr.utarwyn.endercontainers.enderchest.EnderChestManager;
import org.assertj.core.api.Condition;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EnderChestInventoryTest {

    private EnderChestInventory inventory;

    @Mock
    private EnderChest chest;

    @BeforeEach
    public void setUp() throws TestInitializationException {
        TestHelper.setUpFiles();

        UUID uuid = TestHelper.getPlayer().getUniqueId();
        when(this.chest.getOwner()).thenReturn(uuid);
        when(this.chest.getRows()).thenReturn(3);
        when(this.chest.getMaxSize()).thenReturn(27);
        when(this.chest.getContents()).thenReturn(new ConcurrentHashMap<>());

        this.inventory = new EnderChestInventory(chest);
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
        this.inventory.getInventory().setContents(containerItems);

        // inventory and internal map empty
        assertThat(this.inventory.getContents()).isEmpty();
        assertThat(this.inventory.getInventory().getContents()).containsOnlyNulls();

        // Call the real method here!
        this.inventory.prepare();

        // all items that are in bounds must be added to the container, but all in the internal map
        assertThat(this.inventory.getContents())
                .isNotEmpty().hasSize(3)
                .contains(entry(4, item1), entry(7, item2), entry(38, item3));

        assertThat(this.inventory.getInventory().getContents())
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

        // Reload items of the chest
        when(this.chest.getContents()).thenReturn(new ConcurrentHashMap<Integer, ItemStack>() {{
            put(1, new ItemStack(Material.DIAMOND, 3));
            // This item should be replaced by the one in the fake container:
            put(9, new ItemStack(Material.GOLDEN_AXE, 1));
            put(35, new ItemStack(Material.SPRUCE_FENCE, 16));
        }});

        this.inventory.prepare();
        this.inventory.getInventory().setContents(itemList);
        this.inventory.updateContentsFromContainer();

        Map<Integer, ItemStack> map = this.inventory.getContents();

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
    public void saveOnClose() throws TestInitializationException {
        EnderChestManager manager = mock(EnderChestManager.class);
        Player viewer = mock(Player.class);
        Location location = mock(Location.class);

        when(viewer.getWorld()).thenReturn(mock(World.class));
        when(viewer.getLocation()).thenReturn(location);
        TestHelper.registerManagers(manager);

        // do not save if the owner is connected
        this.inventory.onClose(viewer);
        verify(manager, never()).savePlayerContext(any());
        verify(manager, never()).deletePlayerContextIfUnused(this.chest.getOwner());

        // save if forced by the configuration
        TestHelper.overrideConfigurationValue("saveMode", SaveMode.ON_CLOSE);
        this.inventory.onClose(viewer);
        verify(manager).savePlayerContext(this.chest.getOwner());
        verify(manager, never()).deletePlayerContextIfUnused(this.chest.getOwner());
        TestHelper.overrideConfigurationValue("saveMode", SaveMode.LOGOUT);

        // save if owner not connected
        UUID offline = UUID.fromString("62dcb385-f2ac-472f-9d88-a0cc0d957082");
        when(this.chest.getOwner()).thenReturn(offline);
        this.inventory.onClose(viewer);
        verify(manager).savePlayerContext(this.chest.getOwner());
        verify(manager).deletePlayerContextIfUnused(this.chest.getOwner());
    }

    @Test
    public void globalSoundOnClose() {
        Player viewer = mock(Player.class);
        Location location = mock(Location.class);
        World world = mock(World.class);

        when(viewer.getWorld()).thenReturn(world);
        when(viewer.getLocation()).thenReturn(location);

        this.inventory.onClose(viewer);
        verify(world).playSound(location, Sound.BLOCK_CHEST_CLOSE, 1f, 1f);
    }

    @Test
    public void playerSoundOnClose() throws TestInitializationException {
        Player viewer = mock(Player.class);
        Location location = mock(Location.class);

        when(viewer.getLocation()).thenReturn(location);

        TestHelper.overrideConfigurationValue("globalSound", false);
        this.inventory.onClose(viewer);
        verify(viewer).playSound(location, Sound.BLOCK_CHEST_CLOSE, 1f, 1f);
    }

}

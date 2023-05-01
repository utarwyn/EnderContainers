package fr.utarwyn.endercontainers.enderchest.listener;

import com.google.common.collect.ImmutableMap;
import fr.utarwyn.endercontainers.TestHelper;
import fr.utarwyn.endercontainers.TestInitializationException;
import fr.utarwyn.endercontainers.enderchest.EnderChestManager;
import fr.utarwyn.endercontainers.inventory.EnderChestInventory;
import fr.utarwyn.endercontainers.inventory.InventoryManager;
import org.bukkit.*;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyFloat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class EnderChestInventoryListenerTest {

    private EnderChestInventoryListener listener;

    @Mock
    private EnderChestManager manager;

    @Mock
    private InventoryManager inventoryManager;

    @Mock
    private EnderChestInventory enderChestInventory;

    @Mock
    private Inventory inventory;

    @Mock
    private InventoryView inventoryView;

    @Mock
    private Player player;

    @Mock
    private World world;

    @Before
    public void setUp() throws TestInitializationException {
        TestHelper.registerManagers(this.manager, this.inventoryManager);
        TestHelper.setUpFiles();

        this.listener = new EnderChestInventoryListener(this.manager);

        doCallRealMethod().when(this.inventoryManager).cancelClickEventIfRestricted(any(), any());
        lenient().doCallRealMethod().when(this.inventoryManager).cancelDragEventIfRestricted(any(), any());

        when(this.player.getWorld()).thenReturn(this.world);
        when(this.player.getLocation()).thenReturn(new Location(this.world, 0, 0, 0));
        when(this.player.getGameMode()).thenReturn(GameMode.SURVIVAL);
        when(this.inventory.getHolder()).thenReturn(this.enderChestInventory);
        when(this.inventory.getSize()).thenReturn(27);
        when(this.inventory.getType()).thenReturn(InventoryType.CHEST);
        when(this.inventoryView.getTopInventory()).thenReturn(this.inventory);
        when(this.inventoryView.getPlayer()).thenReturn(this.player);
    }

    @Test
    public void inventoryClickVanillaChest() {
        when(this.inventory.getType()).thenReturn(InventoryType.ENDER_CHEST);

        InventoryClickEvent event = createInventoryClickEvent(false);

        // No item
        this.listener.onInventoryClick(event);
        assertThat(event.isCancelled()).isFalse();

        // Item not forbidden
        when(this.inventoryView.getCursor()).thenReturn(new ItemStack(Material.ARROW));
        this.listener.onInventoryClick(event);
        assertThat(event.isCancelled()).isFalse();

        // An item forbidden in the configuration
        when(this.inventoryView.getCursor()).thenReturn(new ItemStack(Material.BEDROCK));
        this.listener.onInventoryClick(event);
        assertThat(event.isCancelled()).isTrue();
    }

    @Test
    public void inventoryClickEnderChest() {
        InventoryClickEvent event = createInventoryClickEvent(false);

        // No item
        this.listener.onInventoryClick(event);
        assertThat(event.isCancelled()).isFalse();

        // Item not forbidden
        when(this.inventoryView.getCursor()).thenReturn(new ItemStack(Material.ARROW));
        this.listener.onInventoryClick(event);
        assertThat(event.isCancelled()).isFalse();

        // An item forbidden in the configuration
        when(this.inventoryView.getCursor()).thenReturn(new ItemStack(Material.OAK_BOAT));
        this.listener.onInventoryClick(event);
        assertThat(event.isCancelled()).isTrue();
    }

    @Test
    public void inventoryShiftClick() {
        InventoryClickEvent event = createInventoryClickEvent(true);

        // No item
        this.listener.onInventoryClick(event);
        assertThat(event.isCancelled()).isFalse();

        // Item not forbidden
        when(this.inventoryView.getItem(2)).thenReturn(new ItemStack(Material.ARROW));
        this.listener.onInventoryClick(event);
        assertThat(event.isCancelled()).isFalse();

        // An item forbidden in the configuration
        when(this.inventoryView.getItem(2)).thenReturn(new ItemStack(Material.BEDROCK));
        this.listener.onInventoryClick(event);
        assertThat(event.isCancelled()).isTrue();
    }

    @Test
    public void inventoryDragVanillaChest() {
        when(this.inventory.getType()).thenReturn(InventoryType.ENDER_CHEST);

        InventoryDragEvent event = createInventoryDragEvent(new HashMap<>());

        // No item
        this.listener.onInventoryDrag(event);
        assertThat(event.isCancelled()).isFalse();

        // Items not forbidden
        event = createInventoryDragEvent(ImmutableMap.of(25, new ItemStack(Material.STONE)));
        this.listener.onInventoryDrag(event);
        assertThat(event.isCancelled()).isFalse();

        // An item forbidden in the configuration
        event = createInventoryDragEvent(ImmutableMap.of(14, new ItemStack(Material.BEDROCK)));
        this.listener.onInventoryDrag(event);
        assertThat(event.isCancelled()).isTrue();
    }

    @Test
    public void inventoryDragEnderChest() {
        InventoryDragEvent event = createInventoryDragEvent(new HashMap<>());

        // No item
        this.listener.onInventoryDrag(event);
        assertThat(event.isCancelled()).isFalse();

        // Items not forbidden
        event = createInventoryDragEvent(ImmutableMap.of(25, new ItemStack(Material.STONE)));
        this.listener.onInventoryDrag(event);
        assertThat(event.isCancelled()).isFalse();

        // An item forbidden in the configuration
        event = createInventoryDragEvent(ImmutableMap.of(14, new ItemStack(Material.OAK_BOAT)));
        this.listener.onInventoryDrag(event);
        assertThat(event.isCancelled()).isTrue();
    }

    @Test
    public void inventoryCloseGlobalSound() {
        when(this.inventory.getType()).thenReturn(InventoryType.ENDER_CHEST);
        this.listener.onInventoryClose(new InventoryCloseEvent(this.inventoryView));
        verify(this.world).playSound(this.player.getLocation(), Sound.BLOCK_CHEST_CLOSE, 1f, 1f);
    }

    @Test
    public void inventoryClosePlayerSound() throws TestInitializationException {
        when(this.inventory.getType()).thenReturn(InventoryType.ENDER_CHEST);
        TestHelper.overrideConfigurationValue("globalSound", false);

        this.listener.onInventoryClose(new InventoryCloseEvent(this.inventoryView));
        verify(this.player).playSound(this.player.getLocation(), Sound.BLOCK_CHEST_CLOSE, 1f, 1f);
    }

    @Test
    public void inventoryCloseUnsupportedActions() {
        when(this.inventory.getType()).thenReturn(InventoryType.ENDER_CHEST);
        InventoryCloseEvent event = new InventoryCloseEvent(this.inventoryView);

        // try with an unknown entity -> no sound
        when(event.getPlayer()).thenReturn(mock(HumanEntity.class));
        this.listener.onInventoryClose(event);
        verify(this.player, never()).playSound(any(Location.class), any(Sound.class), anyFloat(), anyFloat());

        // try with another type of container -> no sound
        when(event.getPlayer()).thenReturn(this.player);
        when(event.getInventory().getType()).thenReturn(InventoryType.CHEST);
        this.listener.onInventoryClose(event);
        verify(this.player, never()).playSound(any(Location.class), any(Sound.class), anyFloat(), anyFloat());

        // try with an enderchest managed by the plugin -> no sound (integrated in the inventory system)
        when(event.getInventory().getType()).thenReturn(InventoryType.ENDER_CHEST);
        this.listener.onInventoryClose(event);
        verify(this.player, never()).playSound(any(Location.class), any(Sound.class), anyFloat(), anyFloat());
    }

    private InventoryClickEvent createInventoryClickEvent(boolean shift) {
        return new InventoryClickEvent(
                this.inventoryView, InventoryType.SlotType.CONTAINER, 2,
                shift ? ClickType.SHIFT_LEFT : ClickType.LEFT, InventoryAction.NOTHING
        );
    }

    private InventoryDragEvent createInventoryDragEvent(Map<Integer, ItemStack> slots) {
        return new InventoryDragEvent(
                this.inventoryView, null,
                new ItemStack(Material.STONE), false, slots
        );
    }

}

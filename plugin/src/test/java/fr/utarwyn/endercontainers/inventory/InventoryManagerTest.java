package fr.utarwyn.endercontainers.inventory;

import com.google.common.collect.ImmutableMap;
import fr.utarwyn.endercontainers.EnderContainers;
import fr.utarwyn.endercontainers.TestHelper;
import fr.utarwyn.endercontainers.TestInitializationException;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class InventoryManagerTest {

    private InventoryManager manager;

    @Mock
    private AbstractInventoryHolder holder;

    @Mock
    private Inventory inventory;

    @Mock
    private InventoryView inventoryView;

    @Mock
    private Player player;

    @Before
    public void setUp() {
        this.manager = new InventoryManager();

        when(this.player.getGameMode()).thenReturn(GameMode.SURVIVAL);
        when(this.inventory.getHolder()).thenReturn(this.holder);
        when(this.inventoryView.getTopInventory()).thenReturn(this.inventory);
        when(this.inventoryView.getPlayer()).thenReturn(this.player);
    }

    @Test
    public void initialize() throws TestInitializationException {
        TestHelper.setupManager(this.manager);

        this.manager.initialize();

        // Verify that the manager has been registered
        verify(Bukkit.getServer().getPluginManager())
                .registerEvents(eq(this.manager), any(EnderContainers.class));
    }

    @Test
    public void inventoryClickInside() {
        when(this.inventory.getSize()).thenReturn(27);

        InventoryClickEvent event = new InventoryClickEvent(
                this.inventoryView, InventoryType.SlotType.CONTAINER, 2,
                ClickType.LEFT, InventoryAction.NOTHING
        );

        // Default behavior (no interaction)
        when(this.holder.canMoveItemInside(any())).thenReturn(false);
        this.manager.onInventoryClick(event);
        assertThat(event.isCancelled()).isTrue();
        verify(holder).onClick(player, event.getRawSlot());

        // With interaction allowed
        when(this.holder.canMoveItemInside(any())).thenReturn(true);
        this.manager.onInventoryClick(event);
        assertThat(event.isCancelled()).isFalse();
        verify(holder, times(2)).onClick(player, event.getRawSlot());
    }

    @Test
    public void inventoryClickOutside() {
        when(this.inventory.getSize()).thenReturn(27);

        // With an unvalid slot position in the inventory
        InventoryClickEvent event = new InventoryClickEvent(
                this.inventoryView, InventoryType.SlotType.CONTAINER, -1,
                ClickType.LEFT, InventoryAction.NOTHING
        );

        when(this.holder.canMoveItemInside(any())).thenReturn(false);
        this.manager.onInventoryClick(event);
        assertThat(event.isCancelled()).isFalse();

        // With an unvalid slot position in the inventory + a shift clic
        event = new InventoryClickEvent(
                this.inventoryView, InventoryType.SlotType.CONTAINER, 36,
                ClickType.SHIFT_LEFT, InventoryAction.NOTHING
        );

        this.manager.onInventoryClick(event);
        assertThat(event.isCancelled()).isTrue();
        verify(holder, never()).onClick(player, event.getRawSlot());

        // Without a custom inventory
        when(this.inventory.getHolder()).thenReturn(null);
        event = new InventoryClickEvent(
                this.inventoryView, InventoryType.SlotType.CONTAINER, -1,
                ClickType.LEFT, InventoryAction.NOTHING
        );

        this.manager.onInventoryClick(event);
        assertThat(event.getResult()).isEqualTo(Event.Result.DEFAULT);
    }

    @Test
    public void inventoryClickSpectateMode() {
        when(this.inventory.getSize()).thenReturn(27);

        InventoryClickEvent event = new InventoryClickEvent(
                this.inventoryView, InventoryType.SlotType.CONTAINER, 2,
                ClickType.LEFT, InventoryAction.NOTHING
        );

        // Event cancelled but click triggered in spectate mode
        when(this.player.getGameMode()).thenReturn(GameMode.SPECTATOR);
        this.manager.onInventoryClick(event);
        assertThat(event.isCancelled()).isTrue();
        verify(holder).onClick(player, event.getRawSlot());
    }

    @Test
    public void inventoryDragInside() {
        when(this.inventory.getSize()).thenReturn(27);

        InventoryDragEvent event = new InventoryDragEvent(
                this.inventoryView, null, new ItemStack(Material.STONE), false,
                ImmutableMap.of(25, new ItemStack(Material.STONE), 34, new ItemStack(Material.STONE))
        );

        // With the restriction enabled
        when(this.holder.canMoveItemInside(any())).thenReturn(false);
        this.manager.onInventoryDrag(event);
        assertThat(event.isCancelled()).isTrue();

        event.setResult(Event.Result.DEFAULT); // reset the event result

        // With the restriction disabled
        when(this.holder.canMoveItemInside(any())).thenReturn(true);
        this.manager.onInventoryDrag(event);
        assertThat(event.isCancelled()).isFalse();
    }

    @Test
    public void inventoryDragOutside() {
        when(this.inventory.getSize()).thenReturn(27);

        InventoryDragEvent event = new InventoryDragEvent(
                this.inventoryView, null, new ItemStack(Material.STONE), false,
                ImmutableMap.of(32, new ItemStack(Material.STONE), 33, new ItemStack(Material.STONE))
        );

        // With the restriction enabled
        when(this.holder.canMoveItemInside(any())).thenReturn(false);
        this.manager.onInventoryDrag(event);
        assertThat(event.isCancelled()).isFalse();

        // With the restriction disabled
        when(this.holder.canMoveItemInside(any())).thenReturn(true);
        this.manager.onInventoryDrag(event);
        assertThat(event.isCancelled()).isFalse();

        // Without a custom inventory
        when(this.inventory.getHolder()).thenReturn(null);
        event = new InventoryDragEvent(
                this.inventoryView, null, new ItemStack(Material.STONE), false,
                ImmutableMap.of()
        );

        this.manager.onInventoryDrag(event);
        assertThat(event.getResult()).isEqualTo(Event.Result.DEFAULT);
    }

    @Test
    public void inventoryDragSpectateMode() {
        when(this.inventory.getSize()).thenReturn(27);

        InventoryDragEvent event = new InventoryDragEvent(
                this.inventoryView, null, new ItemStack(Material.STONE), false,
                ImmutableMap.of(25, new ItemStack(Material.STONE), 34, new ItemStack(Material.STONE))
        );

        when(this.player.getGameMode()).thenReturn(GameMode.SPECTATOR);
        this.manager.onInventoryDrag(event);
        assertThat(event.isCancelled()).isTrue();
    }

    @Test
    public void inventoryClose() {
        InventoryCloseEvent event = new InventoryCloseEvent(inventoryView);

        this.manager.onInventoryClose(event);
        verify(this.holder).onClose(player);
    }

    @Test
    public void closeAll() {
        Inventory enderchestInv = mock(Inventory.class);
        InventoryView enderchestView = mock(InventoryView.class);
        Player player = mock(Player.class);

        when(player.getOpenInventory()).thenReturn(this.inventoryView);

        TestHelper.setUpServer();
        doReturn(Collections.singletonList(player)).when(Bukkit.getServer()).getOnlinePlayers();

        // Check that all inventories are closed by the method
        this.manager.closeAll();
        verify(player).closeInventory();
        verify(this.holder).onClose(player);

        // Check also that vanilla enderchest inventories are closed
        when(enderchestInv.getType()).thenReturn(InventoryType.ENDER_CHEST);
        when(enderchestView.getTopInventory()).thenReturn(enderchestInv);
        when(player.getOpenInventory()).thenReturn(enderchestView);

        this.manager.closeAll();
        verify(player, times(2)).closeInventory();
    }

}

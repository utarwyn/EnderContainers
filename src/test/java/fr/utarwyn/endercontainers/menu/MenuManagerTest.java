package fr.utarwyn.endercontainers.menu;

import fr.utarwyn.endercontainers.TestHelper;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class MenuManagerTest {

    private MenuManager menuManager;

    @Before
    public void setUp() {
        this.menuManager = new MenuManager();
    }

    @Test
    public void inventoryClick() {
        AbstractMenu menu = mock(AbstractMenu.class);
        Inventory inventory = mock(Inventory.class);
        InventoryView view = mock(InventoryView.class);
        Player player = mock(Player.class);

        when(inventory.getHolder()).thenReturn(menu);
        when(view.getTopInventory()).thenReturn(inventory);
        when(view.getPlayer()).thenReturn(player);
        when(inventory.getSize()).thenReturn(54);

        InventoryClickEvent event = new InventoryClickEvent(
                view, InventoryType.SlotType.CONTAINER, 2,
                ClickType.LEFT, InventoryAction.NOTHING
        );

        // Default behavior without item
        this.menuManager.onInventoryClick(event);
        assertThat(event.isCancelled()).isFalse();

        // Default event without cancellation
        when(view.getItem(event.getRawSlot())).thenReturn(new ItemStack(Material.STONE));
        this.menuManager.onInventoryClick(event);
        verify(menu, times(1)).onClick(player, event.getSlot());
        assertThat(event.isCancelled()).isFalse();

        // Cancelled event
        when(menu.onClick(player, event.getSlot())).thenReturn(true);
        this.menuManager.onInventoryClick(event);
        assertThat(event.isCancelled()).isTrue();
    }

    @Test
    public void inventoryClose() {
        AbstractMenu menu = mock(AbstractMenu.class);
        Inventory inventory = mock(Inventory.class);
        InventoryView view = mock(InventoryView.class);
        Player player = mock(Player.class);

        when(inventory.getHolder()).thenReturn(menu);
        when(view.getTopInventory()).thenReturn(inventory);
        when(view.getPlayer()).thenReturn(player);

        InventoryCloseEvent event = new InventoryCloseEvent(view);

        this.menuManager.onInventoryClose(event);
        verify(menu, times(1)).onClose(player);
    }

    @Test
    public void closeAll() {
        AbstractMenu menu = mock(AbstractMenu.class);
        Inventory menuInv = mock(Inventory.class);
        InventoryView menuView = mock(InventoryView.class);
        Inventory enderchestInv = mock(Inventory.class);
        InventoryView enderchestView = mock(InventoryView.class);

        Player player = mock(Player.class);

        when(menuInv.getHolder()).thenReturn(menu);
        when(menuView.getTopInventory()).thenReturn(menuInv);
        when(player.getOpenInventory()).thenReturn(menuView);

        TestHelper.setUpServer();
        doReturn(Collections.singletonList(player)).when(Bukkit.getServer()).getOnlinePlayers();

        // Check that all menus are closed by the method
        this.menuManager.closeAll();
        verify(player, times(1)).closeInventory();
        verify(menu, times(1)).onClose(player);

        // Check also that enderchest inventories are closed
        when(enderchestInv.getType()).thenReturn(InventoryType.ENDER_CHEST);
        when(enderchestView.getTopInventory()).thenReturn(enderchestInv);
        when(player.getOpenInventory()).thenReturn(enderchestView);

        this.menuManager.closeAll();
        verify(player, times(2)).closeInventory();
    }

}

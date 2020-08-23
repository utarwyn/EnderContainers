package fr.utarwyn.endercontainers.menu;

import fr.utarwyn.endercontainers.TestHelper;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class AbstractMenuTest {

    @Mock(answer = Answers.CALLS_REAL_METHODS)
    private AbstractMenu menu;

    @Mock
    private Inventory inventory;

    @BeforeClass
    public static void setUpClass() {
        TestHelper.setUpServer();
    }

    @Test
    public void itemMovingRestricted() {
        this.menu.itemMovingRestricted = true;
        assertThat(this.menu.isItemMovingRestricted()).isTrue();
        this.menu.itemMovingRestricted = false;
        assertThat(this.menu.isItemMovingRestricted()).isFalse();
    }

    @Test
    public void isUsed() {
        // With no inventory
        assertThat(this.menu.isUsed()).isFalse();
        this.menu.inventory = this.inventory;

        // With an inventory, simulate a viewer
        assertThat(this.menu.isUsed()).isFalse();
        when(this.inventory.getViewers()).thenReturn(Collections.singletonList(mock(Player.class)));
        assertThat(this.menu.isUsed()).isTrue();
    }

    @Test
    public void inventory() {
        assertThat(this.menu.inventory).isNull();
        this.menu.inventory = this.inventory;
        assertThat(this.menu.getInventory()).isNotNull().isEqualTo(this.menu.inventory);
    }

    @Test
    public void reloadInventory() {
        int rows = 5;
        String title = "very long default inventory title";

        when(this.menu.getRows()).thenReturn(rows);
        when(this.menu.getTitle()).thenReturn(title);
        assertThat(this.menu.isInitialized()).isFalse();

        // Create a new inventory
        this.menu.reloadInventory();

        assertThat(this.menu.inventory).isNotNull();
        assertThat(this.menu.isInitialized()).isTrue();

        verify(this.menu).prepare();
        verify(Bukkit.getServer()).createInventory(this.menu, rows * 9, title.substring(0, 32));

        // Reload an inventory with itemstacks
        ItemStack[] itemList = this.getFakeItemList();

        when(this.menu.inventory.getContents()).thenReturn(itemList);
        this.menu.reloadInventory();
        verify(this.menu.inventory).setContents(itemList);
    }

    @Test
    public void open() {
        Player player = mock(Player.class);

        this.menu.inventory = this.inventory;

        this.menu.open(player);
        this.menu.onClick(player, 0);

        verify(player).openInventory(this.inventory);
    }

    @Test
    public void close() {
        Player player = mock(Player.class);

        this.menu.inventory = this.inventory;
        when(this.menu.inventory.getViewers()).thenReturn(Collections.singletonList(player));

        this.menu.close();

        verify(player).closeInventory();
    }

    @Test
    public void filledSlotsNb() {
        this.menu.inventory = this.inventory;

        when(this.menu.inventory.getContents()).thenReturn(this.getFakeItemList());
        assertThat(this.menu.getFilledSlotsNb()).isEqualTo(3);
    }

    /**
     * Create a fake item list to put into mocked inventories.
     *
     * @return item list with a size of 10, but with 3 real itemstacks.
     */
    private ItemStack[] getFakeItemList() {
        ItemStack[] itemList = new ItemStack[10];
        itemList[2] = new ItemStack(Material.ENDER_CHEST);
        itemList[8] = new ItemStack(Material.ENDER_CHEST);
        itemList[9] = new ItemStack(Material.ENDER_CHEST);
        return itemList;
    }

}

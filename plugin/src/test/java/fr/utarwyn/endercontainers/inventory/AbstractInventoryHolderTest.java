package fr.utarwyn.endercontainers.inventory;

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
public class AbstractInventoryHolderTest {

    @Mock(answer = Answers.CALLS_REAL_METHODS)
    private AbstractInventoryHolder holder;

    @Mock
    private Inventory inventory;

    @BeforeClass
    public static void setUpClass() {
        TestHelper.setUpServer();
    }

    @Test
    public void itemMovingRestricted() {
        this.holder.itemMovingRestricted = true;
        assertThat(this.holder.isItemMovingRestricted()).isTrue();
        this.holder.itemMovingRestricted = false;
        assertThat(this.holder.isItemMovingRestricted()).isFalse();
    }

    @Test
    public void isUsed() {
        // With no inventory
        assertThat(this.holder.isUsed()).isFalse();
        this.holder.inventory = this.inventory;

        // With an inventory, simulate a viewer
        assertThat(this.holder.isUsed()).isFalse();
        when(this.inventory.getViewers()).thenReturn(Collections.singletonList(mock(Player.class)));
        assertThat(this.holder.isUsed()).isTrue();
    }

    @Test
    public void inventory() {
        assertThat(this.holder.inventory).isNull();
        this.holder.inventory = this.inventory;
        assertThat(this.holder.getInventory()).isNotNull().isEqualTo(this.holder.inventory);
    }

    @Test
    public void reloadInventory() {
        int rows = 5;
        String title = "very long default inventory title";

        when(this.holder.getRows()).thenReturn(rows);
        when(this.holder.getTitle()).thenReturn(title);
        assertThat(this.holder.isInitialized()).isFalse();

        // Create a new inventory
        this.holder.reloadInventory();

        assertThat(this.holder.inventory).isNotNull();
        assertThat(this.holder.isInitialized()).isTrue();

        verify(this.holder).prepare();
        verify(Bukkit.getServer()).createInventory(this.holder, rows * 9, title.substring(0, 32));

        // Reload an inventory with itemstacks
        ItemStack[] itemList = this.getFakeItemList();

        when(this.holder.inventory.getContents()).thenReturn(itemList);
        this.holder.reloadInventory();
        verify(this.holder.inventory).setContents(itemList);
    }

    @Test
    public void open() {
        Player player = mock(Player.class);

        this.holder.inventory = this.inventory;

        this.holder.open(player);
        this.holder.onClick(player, 0);

        verify(player).openInventory(this.inventory);
    }

    @Test
    public void close() {
        Player player = mock(Player.class);

        this.holder.inventory = this.inventory;
        when(this.holder.inventory.getViewers()).thenReturn(Collections.singletonList(player));

        this.holder.close();

        verify(player).closeInventory();
    }

    @Test
    public void filledSlotsNb() {
        this.holder.inventory = this.inventory;

        when(this.holder.inventory.getContents()).thenReturn(this.getFakeItemList());
        assertThat(this.holder.getFilledSlotsNb()).isEqualTo(3);
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

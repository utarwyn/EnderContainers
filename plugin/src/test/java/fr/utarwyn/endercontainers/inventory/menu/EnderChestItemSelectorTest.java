package fr.utarwyn.endercontainers.inventory.menu;

import fr.utarwyn.endercontainers.TestHelper;
import fr.utarwyn.endercontainers.TestInitializationException;
import fr.utarwyn.endercontainers.configuration.ui.EnderChestItem;
import fr.utarwyn.endercontainers.enderchest.EnderChest;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class EnderChestItemSelectorTest {

    private EnderChest enderChest;

    @BeforeClass
    public static void setUpClass() throws TestInitializationException {
        TestHelper.setUpFiles();
    }

    @Before
    public void setUp() {
        this.enderChest = mock(EnderChest.class);
        when(this.enderChest.isAccessible()).thenReturn(true);
    }

    @Test
    public void selectDefault() {
        EnderChestItem item = new EnderChestItemSelector().fromEnderchest(enderChest);
        assertThat(item.getName()).isEqualTo("{{menus.chest_item_accessible_title}}");
        assertThat(item.getType()).isEqualTo("LIME_STAINED_GLASS_PANE");
    }

    @Test
    public void selectChestFilling() {
        when(enderChest.getFillPercentage()).thenReturn(0.95);
        EnderChestItem item = new EnderChestItemSelector().fromEnderchest(enderChest);
        assertThat(item.getType()).isEqualTo("RED_STAINED_GLASS_PANE");
    }

    @Test
    public void selectChestInaccessible() {
        when(enderChest.isAccessible()).thenReturn(false);
        EnderChestItem item = new EnderChestItemSelector().fromEnderchest(enderChest);
        assertThat(item.getName()).isEqualTo("{{menus.chest_item_inaccessible_title}}");
    }

    @Test
    public void selectChestNumber() {
        when(enderChest.getNum()).thenReturn(3);
        EnderChestItem item = new EnderChestItemSelector().fromEnderchest(enderChest);
        assertThat(item.getName()).isEqualTo("Third chest");
    }

}

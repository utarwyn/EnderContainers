package fr.utarwyn.endercontainers.inventory.menu;

import fr.utarwyn.endercontainers.TestHelper;
import fr.utarwyn.endercontainers.TestInitializationException;
import fr.utarwyn.endercontainers.configuration.ui.EnderChestItem;
import fr.utarwyn.endercontainers.enderchest.EnderChest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EnderChestItemSelectorTest {

    private EnderChest enderChest;

    @BeforeAll
    static void setUpClass() throws TestInitializationException {
        TestHelper.setUpFiles();
    }

    @BeforeEach
    void setUp() {
        this.enderChest = mock(EnderChest.class);
        when(this.enderChest.isAccessible()).thenReturn(true);
    }

    @Test
    void selectDefault() {
        EnderChestItem item = new EnderChestItemSelector().fromEnderchest(enderChest);
        assertThat(item.getName()).isEqualTo("{{menus.chest_item_accessible_title}}");
        assertThat(item.getType()).isEqualTo("LIME_STAINED_GLASS_PANE");
    }

    @Test
    void selectChestFilling() {
        when(enderChest.getFillPercentage()).thenReturn(0.95);
        EnderChestItem item = new EnderChestItemSelector().fromEnderchest(enderChest);
        assertThat(item.getType()).isEqualTo("RED_STAINED_GLASS_PANE");
    }

    @Test
    void selectChestInaccessible() {
        when(enderChest.isAccessible()).thenReturn(false);
        EnderChestItem item = new EnderChestItemSelector().fromEnderchest(enderChest);
        assertThat(item.getName()).isEqualTo("{{menus.chest_item_inaccessible_title}}");
    }

    @Test
    void selectChestNumber() {
        when(enderChest.getNum()).thenReturn(3);
        EnderChestItem item = new EnderChestItemSelector().fromEnderchest(enderChest);
        assertThat(item.getName()).isEqualTo("Third chest");
    }

}

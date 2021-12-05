package fr.utarwyn.endercontainers.enderchest;

import fr.utarwyn.endercontainers.TestHelper;
import fr.utarwyn.endercontainers.enderchest.context.PlayerContext;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class VanillaEnderChestTest {

    @Mock
    private PlayerContext context;

    @Mock
    private Inventory chestInventory;

    private VanillaEnderChest chest;

    @Before
    public void setUp() {
        Player player = TestHelper.getPlayer();
        UUID uuid = player.getUniqueId();

        when(this.context.getOwner()).thenReturn(uuid);
        when(this.context.getOwnerAsObject()).thenReturn(player);
        when(player.getEnderChest()).thenReturn(this.chestInventory);

        this.chest = new VanillaEnderChest(this.context);
    }

    @Test
    public void initialize() {
        assertThat(this.chest.container).isNull();
        assertThat(this.chest.getNum()).isEqualTo(0);
        assertThat(this.chest.getRows()).isEqualTo(3);
        assertThat(this.chest.getMaxSize()).isEqualTo(27);
        assertThat(this.chest.getOwner()).isEqualTo(TestHelper.getPlayer().getUniqueId());
    }

    @Test
    public void updateRowCount() {
        this.chest.updateRowCount();
        assertThat(this.chest.getRows()).isEqualTo(3);
    }

    @Test
    public void doNotUpdateContainer() {
        this.chest.updateContainer();
        assertThat(this.chest.container).isNull();
    }

    @Test
    public void getSize() {
        // Inventory without owner
        when(this.context.getOwnerAsObject()).thenReturn(null);
        assertThat((new VanillaEnderChest(this.context)).getSize()).isZero();

        // Inventory without content
        when(this.chestInventory.getContents()).thenReturn(new ItemStack[0]);
        assertThat(this.chest.getSize()).isZero();

        // Inventory with few items
        ItemStack itemStack = mock(ItemStack.class);
        when(this.chestInventory.getContents()).thenReturn(
                Arrays.asList(itemStack, itemStack, null, itemStack).toArray(new ItemStack[0])
        );
        assertThat(this.chest.getSize()).isEqualTo(3);
    }

    @Test
    public void openContainer() {
        Player viewer = mock(Player.class);
        this.chest.openContainerFor(viewer);
        verify(viewer).openInventory(this.chestInventory);
    }

}

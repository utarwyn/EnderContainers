package fr.utarwyn.endercontainers.enderchest;

import fr.utarwyn.endercontainers.TestHelper;
import fr.utarwyn.endercontainers.TestInitializationException;
import fr.utarwyn.endercontainers.enderchest.context.PlayerContext;
import fr.utarwyn.endercontainers.inventory.EnderChestInventory;
import fr.utarwyn.endercontainers.inventory.InventoryManager;
import fr.utarwyn.endercontainers.storage.player.PlayerData;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EnderChestTest {

    @Mock
    private PlayerData storage;

    @Mock
    private PlayerContext context;

    private EnderChest chest;

    @BeforeEach
    public void setUp() throws TestInitializationException {
        TestHelper.setUpServer();
        TestHelper.setUpFiles();
        TestHelper.registerManagers(mock(InventoryManager.class));

        Player player = TestHelper.getPlayer();
        UUID uuid = player.getUniqueId();

        when(this.storage.getEnderchestContents(any())).thenReturn(new ConcurrentHashMap<>());
        when(this.context.getOwnerAsObject()).thenReturn(player);
        when(this.context.getOwner()).thenReturn(uuid);
        when(this.context.getData()).thenReturn(this.storage);

        this.chest = new EnderChest(this.context, 1);
    }

    @Test
    public void initialState() {
        assertThat(this.chest.container).isNotNull();
        assertThat(this.chest.getNum()).isEqualTo(1);
        assertThat(this.chest.getRows()).isEqualTo(3);
        assertThat(this.chest.getMaxSize()).isEqualTo(27);
        assertThat(this.chest.getOwner()).isEqualTo(TestHelper.getPlayer().getUniqueId());
    }

    @Test
    public void size() {
        // Default size
        assertThat(this.chest.getSize()).isZero();
        assertThat(this.chest.getFillPercentage()).isZero();
        assertThat(this.chest.isEmpty()).isTrue();
        assertThat(this.chest.isFull()).isFalse();

        // With a specific size
        this.chest.container = mock(EnderChestInventory.class);
        when(this.chest.container.getFilledSlotsNb()).thenReturn(6);

        assertThat(this.chest.getSize()).isEqualTo(6);
        assertThat(this.chest.isEmpty()).isFalse();
        assertThat(this.chest.isFull()).isFalse();
        assertThat(this.chest.getFillPercentage()).isEqualTo(6D / this.chest.getMaxSize());

        // When the container is full
        when(this.chest.container.getFilledSlotsNb()).thenReturn(27);
        assertThat(this.chest.isEmpty()).isFalse();
        assertThat(this.chest.isFull()).isTrue();
        assertThat(this.chest.getFillPercentage()).isEqualTo(1);
    }

    @Test
    public void contents() {
        assertThat(this.chest.getContents()).isNotNull().isEmpty();

        // Custom already initialized container
        this.chest.container = mock(EnderChestInventory.class);

        // With non-initialized container
        this.chest.getContents();
        verify(this.storage, times(2)).getEnderchestContents(any());

        // With an existing and loaded container
        ConcurrentMap<Integer, ItemStack> fakeContents = new ConcurrentHashMap<Integer, ItemStack>() {{
            put(10, new ItemStack(Material.STONE));
            put(15, new ItemStack(Material.FIREWORK_ROCKET));
        }};

        when(this.chest.container.isInitialized()).thenReturn(true);
        when(this.chest.container.getContents()).thenReturn(fakeContents);

        assertThat(this.chest.getContents()).isEqualTo(fakeContents);
    }

    @Test
    public void accessibility() {
        EnderChest defaultChest = new EnderChest(this.context, 0);

        // Default behavior
        assertThat(this.chest.isAccessible()).isFalse();
        assertThat(defaultChest.isAccessible()).isTrue();

        // An administrator should have access to the chest of an offline player
        when(this.context.getOwnerAsObject()).thenReturn(null);
        assertThat(this.chest.isAccessible()).isTrue();
        assertThat(defaultChest.isAccessible()).isTrue();
    }

    @Test
    public void container() {
        this.chest.container = mock(EnderChestInventory.class);
        Inventory inventory = mock(Inventory.class);

        when(inventory.getSize()).thenReturn(27);
        when(this.chest.container.getInventory()).thenReturn(inventory);

        // is container used?
        assertThat(this.chest.isContainerUsed()).isFalse();
        when(this.chest.container.isUsed()).thenReturn(true);
        assertThat(this.chest.isContainerUsed()).isTrue();

        // open a container
        Player player = mock(Player.class);

        this.chest.openContainerFor(player);
        verify(this.chest.container).open(player);
    }

}

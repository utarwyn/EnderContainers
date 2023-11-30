package fr.utarwyn.endercontainers.enderchest.context;

import com.google.common.collect.Maps;
import fr.utarwyn.endercontainers.TestHelper;
import fr.utarwyn.endercontainers.TestInitializationException;
import fr.utarwyn.endercontainers.enderchest.VanillaEnderChest;
import fr.utarwyn.endercontainers.inventory.menu.EnderChestListMenu;
import fr.utarwyn.endercontainers.storage.StorageManager;
import fr.utarwyn.endercontainers.storage.player.PlayerData;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PlayerContextTest {

    private static final int ENDERCHEST_AMOUNT = 27;

    private PlayerContext context;

    private Player player;

    @Mock
    private StorageManager storageManager;

    @Mock
    private PlayerData playerData;

    @BeforeAll
    public static void setUpClass() throws TestInitializationException {
        TestHelper.setUpFiles();
    }

    @BeforeEach
    public void setUp() throws TestInitializationException {
        this.player = TestHelper.getPlayer();

        TestHelper.registerManagers(this.storageManager);
        when(this.storageManager.createPlayerDataStorage(this.player.getUniqueId())).thenReturn(this.playerData);
        when(this.playerData.getEnderchestContents(any())).thenReturn(Maps.newConcurrentMap());

        this.context = new PlayerContext(this.player.getUniqueId());
        this.context.loadEnderchests(ENDERCHEST_AMOUNT);
    }

    @Test
    public void create() {
        assertThat(this.context.getOwner()).isEqualTo(this.player.getUniqueId());
        assertThat(this.context.getOwnerAsObject()).isEqualTo(this.player);
        assertThat(this.context.getData()).isEqualTo(this.playerData);
    }

    @Test
    public void offlineOwner() {
        assertThat(new PlayerContext(UUID.randomUUID()).getOwnerAsObject()).isNull();
    }

    @Test
    public void getChest() {
        assertThat(this.context.getChest(0)).isPresent();
        assertThat(this.context.getChest(ENDERCHEST_AMOUNT - 1)).isPresent();
        assertThat(this.context.getChest(ENDERCHEST_AMOUNT)).isNotPresent();
    }

    @Test
    public void getAccessibleChestCount() {
        assertThat(this.context.getAccessibleChestCount()).isEqualTo(1);
        when(this.player.hasPermission(anyString())).thenReturn(true);
        assertThat(this.context.getAccessibleChestCount()).isEqualTo(ENDERCHEST_AMOUNT);
    }

    @Test
    public void isChestsUnused() {
        assertThat(this.context.isChestsUnused()).isTrue();

        when(this.player.getEnderChest().getViewers()).thenReturn(Collections.singletonList(this.player));
        assertThat(this.context.isChestsUnused()).isFalse();
    }

    @Test
    public void loadOfflinePlayerProfile() throws PlayerOfflineLoadException {
        // by default, method can be called but do nothing
        this.context.loadOfflinePlayerProfile();

        // with an offline player, reload first enderchest context
        UUID uuid = TestHelper.FAKE_OFFLINE_UUID;
        when(this.storageManager.createPlayerDataStorage(uuid)).thenReturn(this.playerData);

        this.context = new PlayerContext(uuid);
        this.context.loadEnderchests(1);
        assertThat(this.context.getChest(0)).isPresent();

        VanillaEnderChest chest = (VanillaEnderChest) this.context.getChest(0).get();
        assertThat(chest.getOwnerAsPlayer()).isNull();

        try {
            this.context.loadOfflinePlayerProfile();
        } catch (PlayerOfflineLoadException ignored) {
        }
    }

    @Test
    public void openListInventory() {
        this.context.openListInventory(this.player);
        assertThatPlayerOpenListInventory();
    }

    @Test
    public void openListInventoryWithOneChest() throws TestInitializationException {
        // If only 1 chest accessible, but can see other enderchests, so open list inventory
        TestHelper.overrideConfigurationValue("onlyShowAccessibleEnderchests", false);
        this.context.openListInventory(this.player);
        assertThatPlayerOpenListInventory();
    }

    @Test
    public void openListInventoryWithOneChestAndOthersAreHidden() throws TestInitializationException {
        // If only 1 chest accessible, open it directly
        TestHelper.overrideConfigurationValue("onlyShowAccessibleEnderchests", true);
        this.context.openListInventory(this.player);
        verify(this.player).openInventory(this.player.getEnderChest());
    }

    @Test
    public void openListInventorySound() {
        Block block = mock(Block.class);
        Location location = mock(Location.class);
        World world = mock(World.class);

        when(block.getLocation()).thenReturn(location);
        when(block.getWorld()).thenReturn(world);

        this.context.openListInventory(this.player, block);

        verify(world).playSound(eq(location), eq(Sound.BLOCK_CHEST_OPEN), anyFloat(), anyFloat());
    }

    @Test
    public void openEnderchestInventory() {
        assertThat(this.context.openEnderchestInventory(this.player, 0)).isTrue();
        assertThat(this.context.openEnderchestInventory(this.player, 2)).isFalse();
        when(this.player.hasPermission(anyString())).thenReturn(true);
        assertThat(this.context.openEnderchestInventory(this.player, 2)).isTrue();
        assertThat(this.context.openEnderchestInventory(this.player, ENDERCHEST_AMOUNT)).isFalse();
    }

    @Test
    public void save() {
        this.context.save();
        verify(this.playerData).saveContext(any());
    }

    private void assertThatPlayerOpenListInventory() {
        ArgumentCaptor<Inventory> captor = ArgumentCaptor.forClass(Inventory.class);
        verify(this.player).openInventory(captor.capture());
        assertThat(captor.getValue().getHolder()).isNotNull().isInstanceOf(EnderChestListMenu.class);
    }

}

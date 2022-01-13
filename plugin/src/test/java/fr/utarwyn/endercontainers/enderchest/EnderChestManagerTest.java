package fr.utarwyn.endercontainers.enderchest;

import fr.utarwyn.endercontainers.EnderContainers;
import fr.utarwyn.endercontainers.TestHelper;
import fr.utarwyn.endercontainers.TestInitializationException;
import fr.utarwyn.endercontainers.dependency.DependenciesManager;
import fr.utarwyn.endercontainers.enderchest.context.LoadTask;
import fr.utarwyn.endercontainers.enderchest.context.PlayerContext;
import fr.utarwyn.endercontainers.enderchest.context.SaveTask;
import fr.utarwyn.endercontainers.enderchest.listener.EnderChestListener;
import fr.utarwyn.endercontainers.inventory.InventoryManager;
import fr.utarwyn.endercontainers.storage.StorageManager;
import fr.utarwyn.endercontainers.storage.player.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class EnderChestManagerTest {

    private EnderChestManager manager;

    @BeforeClass
    public static void setUpClass() throws TestInitializationException {
        TestHelper.setUpServer();
        TestHelper.setUpFiles();
    }

    @Before
    public void setUp() {
        this.manager = new EnderChestManager();
        this.manager.load();
    }

    @Test
    public void initialize() throws TestInitializationException {
        DependenciesManager dependenciesManager = mock(DependenciesManager.class);
        InventoryManager inventoryManager = mock(InventoryManager.class);

        TestHelper.registerManagers(dependenciesManager, inventoryManager);
        TestHelper.setupManager(this.manager);

        this.manager.initialize();

        // Verify that the enderchest listener has been registered
        verify(Bukkit.getServer().getPluginManager(), atLeast(1))
                .registerEvents(any(EnderChestListener.class), any(EnderContainers.class));
    }

    @Test
    public void unload() throws TestInitializationException {
        InventoryManager inventoryManager = mock(InventoryManager.class);
        PlayerContext context = mock(PlayerContext.class);

        TestHelper.registerManagers(inventoryManager);
        TestHelper.setupManager(this.manager);

        this.manager.contextMap.put(UUID.randomUUID(), context);

        this.manager.unload();

        verify(context).save();
        verify(inventoryManager).closeAll();
        assertThat(this.manager.contextMap).isEmpty();
    }

    @Test
    public void getMaxEnderchests() {
        // Max amount of enderchests defined in the test config file
        assertThat(this.manager.getMaxEnderchests()).isEqualTo(27);
    }

    @Test
    public void isContextUnused() throws TestInitializationException {
        UUID uuid = UUID.randomUUID();

        TestHelper.setupManager(this.manager);

        assertThat(this.manager.isContextUnused(uuid)).isFalse();

        // Valid context with the default flag
        PlayerContext context = this.registerPlayerContext(uuid);
        assertThat(this.manager.isContextUnused(uuid)).isFalse();

        // Valid context and flag set to true
        when(context.isChestsUnused()).thenReturn(true);
        assertThat(this.manager.isContextUnused(uuid)).isTrue();
    }

    @Test
    public void loadPlayerContext() throws TestInitializationException {
        // Setup the manager correctly
        StorageManager manager = mock(StorageManager.class);
        PlayerData storage = mock(PlayerData.class);

        when(storage.getEnderchestContents(any())).thenReturn(new ConcurrentHashMap<>());
        when(manager.createPlayerDataStorage(any())).thenReturn(storage);

        TestHelper.registerManagers(manager);
        TestHelper.setupManager(this.manager);

        Player player = TestHelper.getPlayer();

        // Load an unregistered context
        Consumer<PlayerContext> consumer = result -> assertThat(result).isNotNull();
        this.manager.loadPlayerContext(player.getUniqueId(), consumer);

        // Reload a registered context
        this.registerPlayerContext(player.getUniqueId());
        this.manager.loadPlayerContext(player.getUniqueId(), consumer);

        // Verify that the scheduler has been called only once (in the first scenario)
        verify(Bukkit.getServer().getScheduler())
                .runTaskAsynchronously(any(), any(LoadTask.class));
    }

    @Test
    public void registerPlayerContext() throws TestInitializationException {
        UUID uuid = UUID.randomUUID();

        TestHelper.setupManager(this.manager);

        // Check before state
        this.manager.loadingContexts.add(uuid);
        assertThat(this.manager.contextMap).isEmpty();

        this.registerPlayerContext(uuid);

        // Check after state
        assertThat(this.manager.contextMap).isNotEmpty().containsOnlyKeys(uuid);
        assertThat(this.manager.loadingContexts).isEmpty();
    }

    @Test
    public void noDuplicateIfContextLoadingIsLong() throws TestInitializationException {
        UUID uuid = UUID.randomUUID();
        PlayerContext playerContext = mock(PlayerContext.class);
        Consumer<PlayerContext> consumer = mock(Consumer.class);

        TestHelper.setupManager(this.manager);

        this.manager.contextMap.put(uuid, playerContext);
        this.manager.loadingContexts.add(uuid);
        this.manager.loadPlayerContext(uuid, consumer);

        // Consumer should be never called due to loading state
        assertThat(this.manager.loadingContexts).contains(uuid);
        verify(consumer, never()).accept(playerContext);
    }

    @Test
    public void savePlayerContext() throws TestInitializationException {
        UUID uuid = UUID.randomUUID();

        // Setup the manager correctly
        TestHelper.setupManager(this.manager);

        // Unregistered context?
        this.manager.savePlayerContext(uuid, false);
        this.registerPlayerContext(uuid);

        // Check saving without deletion
        this.manager.savePlayerContext(uuid, false);
        assertThat(this.manager.contextMap).containsKey(uuid);
        verify(TestHelper.getPlugin()).executeTaskOnOtherThread(any(SaveTask.class));

        // Check deletion of a context
        this.manager.savePlayerContext(uuid, true);
        assertThat(this.manager.contextMap).isEmpty();
    }

    private PlayerContext registerPlayerContext(UUID uuid) {
        PlayerContext context = mock(PlayerContext.class);
        when(context.getOwner()).thenReturn(uuid);

        this.manager.registerPlayerContext(context);
        return context;
    }

}

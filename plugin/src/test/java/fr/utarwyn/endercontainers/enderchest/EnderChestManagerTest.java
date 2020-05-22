package fr.utarwyn.endercontainers.enderchest;

import fr.utarwyn.endercontainers.EnderContainers;
import fr.utarwyn.endercontainers.TestHelper;
import fr.utarwyn.endercontainers.dependency.DependenciesManager;
import fr.utarwyn.endercontainers.enderchest.context.LoadTask;
import fr.utarwyn.endercontainers.enderchest.context.PlayerContext;
import fr.utarwyn.endercontainers.enderchest.context.SaveTask;
import fr.utarwyn.endercontainers.menu.MenuManager;
import fr.utarwyn.endercontainers.storage.StorageManager;
import fr.utarwyn.endercontainers.storage.player.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.Player;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class EnderChestManagerTest {

    private EnderChestManager manager;

    @BeforeClass
    public static void setUpClass() throws IOException,
            InvalidConfigurationException, ReflectiveOperationException {
        TestHelper.setUpServer();
        TestHelper.setUpFiles();
    }

    @Before
    public void setUp() {
        this.manager = new EnderChestManager();
        this.manager.load();
    }

    @Test
    public void initialize() throws ReflectiveOperationException {
        DependenciesManager menuManager = mock(DependenciesManager.class);

        TestHelper.registerManagers(menuManager);
        TestHelper.setupManager(this.manager);

        this.manager.initialize();

        // Verify that the enderchest listener has been registered
        verify(Bukkit.getServer().getPluginManager())
                .registerEvents(any(EnderChestListener.class), any(EnderContainers.class));
    }

    @Test
    public void unload() throws ReflectiveOperationException {
        MenuManager menuManager = mock(MenuManager.class);
        PlayerContext context = mock(PlayerContext.class);

        TestHelper.registerManagers(menuManager);
        TestHelper.setupManager(this.manager);

        this.manager.contextMap.put(UUID.randomUUID(), context);

        this.manager.unload();

        verify(context).save();
        verify(menuManager).closeAll();
        assertThat(this.manager.contextMap).isEmpty();
    }

    @Test
    public void getMaxEnderchests() {
        // Max amount of enderchests defined in the test config file
        assertThat(this.manager.getMaxEnderchests()).isEqualTo(27);
    }

    @Test
    public void isContextUnused() {
        UUID uuid = UUID.randomUUID();

        assertThat(this.manager.isContextUnused(uuid)).isFalse();

        // Valid context with the default flag
        PlayerContext context = this.registerPlayerContext(uuid);
        assertThat(this.manager.isContextUnused(uuid)).isFalse();

        // Valid context and flag set to true
        when(context.isChestsUnused()).thenReturn(true);
        assertThat(this.manager.isContextUnused(uuid)).isTrue();
    }

    @Test
    public void loadPlayerContext() throws ReflectiveOperationException {
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
    public void registerPlayerContext() {
        UUID uuid = UUID.randomUUID();

        assertThat(this.manager.contextMap).isEmpty();
        this.registerPlayerContext(uuid);
        assertThat(this.manager.contextMap).isNotEmpty();
        assertThat(this.manager.contextMap).containsOnlyKeys(uuid);
    }

    @Test
    public void savePlayerContext() throws ReflectiveOperationException {
        UUID uuid = UUID.randomUUID();

        // Setup the manager correctly
        TestHelper.setupManager(this.manager);

        // Unregistered context?
        this.manager.savePlayerContext(uuid, false);
        this.registerPlayerContext(uuid);

        // Check saving without deletion
        this.manager.savePlayerContext(uuid, false);
        assertThat(this.manager.contextMap).containsKey(uuid);

        verify(Bukkit.getServer().getScheduler())
                .runTaskAsynchronously(any(EnderContainers.class), any(SaveTask.class));

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

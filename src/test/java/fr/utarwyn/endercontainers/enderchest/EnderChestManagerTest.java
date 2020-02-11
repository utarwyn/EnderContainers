package fr.utarwyn.endercontainers.enderchest;

import fr.utarwyn.endercontainers.EnderContainers;
import fr.utarwyn.endercontainers.TestHelper;
import fr.utarwyn.endercontainers.dependency.DependenciesManager;
import fr.utarwyn.endercontainers.enderchest.context.ContextRunnable;
import fr.utarwyn.endercontainers.enderchest.context.LoadTask;
import fr.utarwyn.endercontainers.enderchest.context.PlayerContext;
import fr.utarwyn.endercontainers.enderchest.context.SaveTask;
import fr.utarwyn.endercontainers.menu.MenuManager;
import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class EnderChestManagerTest {

    private EnderChestManager manager;

    @BeforeClass
    public static void setUpClass() throws IOException, InvalidConfigurationException {
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
        verify(Bukkit.getServer().getPluginManager(), times(1))
                .registerEvents(any(EnderChestListener.class), any(EnderContainers.class));
    }

    @Test
    public void unload() throws ReflectiveOperationException {
        MenuManager menuManager = mock(MenuManager.class);
        TestHelper.registerManagers(menuManager);

        this.manager.unload();

        verify(menuManager, times(1)).closeAll();
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
        UUID uuid = UUID.randomUUID();

        // Setup the manager correctly
        TestHelper.setupManager(this.manager);

        // Load an unregistered context
        ContextRunnable callback = mock(ContextRunnable.class);
        this.manager.loadPlayerContext(uuid, callback);

        // Reload a registered context
        PlayerContext context = this.registerPlayerContext(uuid);
        this.manager.loadPlayerContext(uuid, callback);
        verify(callback, times(1)).run(context);

        // Verify that the scheduler has been called only once (in the first scenario)
        verify(Bukkit.getServer().getScheduler(), times(1))
                .runTaskAsynchronously(any(EnderContainers.class), any(LoadTask.class));
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

        verify(Bukkit.getServer().getScheduler(), times(1))
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

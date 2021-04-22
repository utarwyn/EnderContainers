package fr.utarwyn.endercontainers.util;

import fr.utarwyn.endercontainers.EnderContainers;
import fr.utarwyn.endercontainers.TestHelper;
import fr.utarwyn.endercontainers.TestInitializationException;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * All tests about {@link Updater} class.
 *
 * @author Utarwyn
 * @since 2.2.1
 */
@RunWith(MockitoJUnitRunner.class)
public class UpdaterTest {

    private Updater updater;

    private EnderContainers plugin;

    private String initialVersion;

    @Mock
    private Player player;

    @BeforeClass
    public static void setUpClass() throws TestInitializationException {
        TestHelper.setUpFiles();
    }

    @Before
    public void setUp() throws TestInitializationException {
        this.updater = new Updater();
        this.plugin = TestHelper.getPlugin();
        TestHelper.setupManager(this.updater);
        this.updater.initialize();
        this.initialVersion = this.plugin.getDescription().getVersion();
    }

    @After
    public void tearDown() {
        lenient().when(this.plugin.getDescription().getVersion()).thenReturn(this.initialVersion);
    }

    @Test
    public void withUpdate() {
        when(this.plugin.getDescription().getVersion()).thenReturn("1.0.0");
        this.updater.load();

        verify(this.plugin.getServer().getScheduler()).runTaskAsynchronously(this.plugin, this.updater);
        this.updater.notifyPlayer(this.player);
        verify(this.player).sendMessage(contains("is not up-to-date"));
    }

    @Test
    public void withoutUpdate() {
        when(this.plugin.getDescription().getVersion()).thenReturn("99.99.99-dev");
        this.updater.load();

        verify(this.plugin.getServer().getScheduler()).runTaskAsynchronously(this.plugin, this.updater);
        this.updater.notifyPlayer(this.player);
        verify(this.player).sendMessage(contains("is up-to-date"));
    }

    @Test
    public void playerJoinUpdateNotification() throws TestInitializationException {
        PlayerJoinEvent event = new PlayerJoinEvent(this.player, "");

        // Register a fake updater
        Updater updater = mock(Updater.class);
        TestHelper.registerManagers(updater);

        // no permission
        this.updater.onPlayerJoin(event);
        verify(this.player, never()).playSound(any(), any(Sound.class), eq(1f), eq(1f));

        // no update
        when(this.player.isOp()).thenReturn(true);
        this.updater.onPlayerJoin(event);
        verify(this.player, never()).playSound(any(), any(Sound.class), eq(1f), eq(1f));

        // update and permission
        when(this.plugin.getDescription().getVersion()).thenReturn("1.0.0");
        this.updater.load();

        this.updater.onPlayerJoin(event);
        verify(this.player).playSound(any(), eq(Sound.BLOCK_NOTE_BLOCK_PLING), eq(1f), eq(1f));
    }

    @Test
    public void unload() {
        this.updater.unload();
    }

}

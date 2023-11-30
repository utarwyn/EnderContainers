package fr.utarwyn.endercontainers.util;

import fr.utarwyn.endercontainers.EnderContainers;
import fr.utarwyn.endercontainers.TestHelper;
import fr.utarwyn.endercontainers.TestInitializationException;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * All tests about {@link Updater} class.
 *
 * @author Utarwyn
 * @since 2.2.1
 */
@ExtendWith(MockitoExtension.class)
public class UpdaterTest {

    private Updater updater;

    private EnderContainers plugin;

    private String initialVersion;

    @Mock
    private Player player;

    @BeforeAll
    public static void setUpClass() throws TestInitializationException {
        TestHelper.setUpFiles();
    }

    @BeforeEach
    public void setUp() throws TestInitializationException {
        this.updater = new Updater();
        this.plugin = TestHelper.getPlugin();
        TestHelper.setupManager(this.updater);
        this.updater.initialize();
        this.initialVersion = this.plugin.getDescription().getVersion();
    }

    @AfterEach
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
        String currentVersion = "99.99.99-dev";
        when(this.plugin.getDescription().getVersion()).thenReturn(currentVersion);
        this.updater.load();

        verify(this.plugin.getServer().getScheduler()).runTaskAsynchronously(this.plugin, this.updater);
        this.updater.notifyPlayer(this.player);
        verify(this.player).sendMessage(contains("is up-to-date"));
        verify(this.player).sendMessage(contains("version is §e" + currentVersion));
    }

    @Test
    public void playerJoinUpdateNotification() throws TestInitializationException {
        PlayerJoinEvent event = new PlayerJoinEvent(this.player, "");

        // Register a fake updater
        Updater updater = mock(Updater.class);
        TestHelper.registerManagers(updater);

        when(this.player.getLocation()).thenReturn(mock(Location.class));

        // no permission
        this.updater.onPlayerJoin(event);
        verify(this.player, never()).playSound(any(Location.class), any(Sound.class), eq(1f), eq(1f));

        // no update
        when(this.player.hasPermission("endercontainers.update")).thenReturn(true);
        this.updater.onPlayerJoin(event);
        verify(this.player, never()).playSound(any(Location.class), any(Sound.class), eq(1f), eq(1f));

        // update and permission
        when(this.plugin.getDescription().getVersion()).thenReturn("1.0.0");
        this.updater.load();

        this.updater.onPlayerJoin(event);
        verify(this.player).playSound(any(Location.class), eq(Sound.BLOCK_NOTE_BLOCK_PLING), eq(1f), eq(1f));
    }

    @Test
    public void unload() {
        this.updater.unload();
    }

}

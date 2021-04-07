package fr.utarwyn.endercontainers.enderchest.context;

import fr.utarwyn.endercontainers.TestHelper;
import fr.utarwyn.endercontainers.TestInitializationException;
import fr.utarwyn.endercontainers.enderchest.EnderChestManager;
import fr.utarwyn.endercontainers.storage.StorageManager;
import fr.utarwyn.endercontainers.storage.player.PlayerData;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class LoadTaskTest {

    private UUID uuid;

    private PlayerContext resultContext;

    @Mock
    private PlayerData playerData;

    @Mock
    private StorageManager storageManager;

    @Mock
    private EnderChestManager chestManager;

    @BeforeClass
    public static void setUpClass() throws TestInitializationException {
        TestHelper.setUpFiles();
    }

    @Before
    public void setUp() throws TestInitializationException {
        TestHelper.registerManagers(this.storageManager);
        TestHelper.setupManager(this.chestManager);
        this.resultContext = null;
        this.uuid = TestHelper.getPlayer().getUniqueId();


        when(this.chestManager.getMaxEnderchests()).thenReturn(27);
    }

    @Test
    public void run() throws TestInitializationException {
        when(playerData.getEnderchestContents(any())).thenReturn(new ConcurrentHashMap<>());
        when(this.storageManager.createPlayerDataStorage(any())).thenReturn(playerData);

        LoadTask task = this.createTask(this.uuid);
        task.run();
        assertThat(this.resultContext).isNotNull();
    }

    @Test
    public void loadError() throws TestInitializationException {
        Logger initialLogger = TestHelper.getPlugin().getLogger();
        Logger logger = mock(Logger.class);
        when(TestHelper.getPlugin().getLogger()).thenReturn(logger);

        when(this.chestManager.getMaxEnderchests()).thenReturn(1);
        LoadTask task = this.createTask(UUID.randomUUID());
        task.run();

        verify(logger).log(eq(Level.SEVERE), eq("cannot load offline player profile"), any(PlayerOfflineLoadException.class));
        lenient().when(TestHelper.getPlugin().getLogger()).thenReturn(initialLogger);
    }

    private LoadTask createTask(UUID owner) throws TestInitializationException {
        return new LoadTask(TestHelper.getPlugin(), this.chestManager, owner, result -> this.resultContext = result);
    }

}

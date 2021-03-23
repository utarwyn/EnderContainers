package fr.utarwyn.endercontainers.storage;

import fr.utarwyn.endercontainers.TestHelper;
import fr.utarwyn.endercontainers.TestInitializationException;
import fr.utarwyn.endercontainers.database.DatabaseManager;
import fr.utarwyn.endercontainers.storage.backups.BackupsFlatData;
import fr.utarwyn.endercontainers.storage.backups.BackupsSQLData;
import fr.utarwyn.endercontainers.storage.player.PlayerFlatData;
import fr.utarwyn.endercontainers.storage.player.PlayerSQLData;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class StorageManagerTest {

    private StorageManager manager;

    @Mock
    private DatabaseManager databaseManager;

    @BeforeClass
    public static void setUpClass() throws TestInitializationException {
        TestHelper.setUpFiles();
    }

    @Before
    public void setUp() throws TestInitializationException {
        this.manager = new StorageManager();
        TestHelper.setupManager(this.manager);
        TestHelper.registerManagers(this.databaseManager);
    }

    @Test
    public void loadFileWrappers() {
        when(this.databaseManager.isReady()).thenReturn(false);
        this.manager.load();

        assertThat(this.manager.createBackupDataStorage())
                .isNotNull()
                .isInstanceOf(BackupsFlatData.class);
        assertThat(this.manager.createPlayerDataStorage(UUID.randomUUID()))
                .isNotNull()
                .isInstanceOf(PlayerFlatData.class);
    }

    @Test
    public void loadSQLWrappers() {
        when(this.databaseManager.isReady()).thenReturn(true);
        this.manager.load();

        assertThat(this.manager.createBackupDataStorage())
                .isNotNull()
                .isInstanceOf(BackupsSQLData.class);
        assertThat(this.manager.createPlayerDataStorage(UUID.randomUUID()))
                .isNotNull()
                .isInstanceOf(PlayerSQLData.class);
    }

}

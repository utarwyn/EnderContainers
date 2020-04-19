package fr.utarwyn.endercontainers.backup;

import fr.utarwyn.endercontainers.TestHelper;
import fr.utarwyn.endercontainers.backup.action.BackupApplyTask;
import fr.utarwyn.endercontainers.backup.action.BackupCreateTask;
import fr.utarwyn.endercontainers.backup.action.BackupRemoveTask;
import fr.utarwyn.endercontainers.storage.StorageManager;
import fr.utarwyn.endercontainers.storage.backups.BackupsData;
import org.bukkit.Bukkit;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class BackupManagerTest {

    private BackupManager manager;

    @Before
    public void setUp() {
        this.manager = new BackupManager();
    }

    @Test
    public void loadAndGetters() throws ReflectiveOperationException {
        StorageManager storageManager = mock(StorageManager.class);
        BackupsData storage = mock(BackupsData.class);
        List<Backup> backupList = new ArrayList<>();

        this.loadManager(storageManager, storage, backupList);

        verify(storageManager).createBackupDataStorage();
        assertThat(this.manager.getStorage()).isEqualTo(storage);
        assertThat(this.manager.getBackups()).isEqualTo(backupList);
    }

    @Test
    public void unload() {
        this.manager.unload();
        assertThat(this.manager.getStorage()).isNull();
    }

    @Test
    public void getBackupByName() throws ReflectiveOperationException {
        Backup backup = mock(Backup.class);
        List<Backup> backupList = new ArrayList<>();

        when(backup.getName()).thenReturn("real");
        backupList.add(backup);

        this.loadManager(mock(StorageManager.class), mock(BackupsData.class), backupList);

        assertThat(this.manager.getBackupByName("real")).isNotEmpty().get().isEqualTo(backup);
        assertThat(this.manager.getBackupByName("fictive")).isEmpty();
    }

    @Test
    public void createBackup() throws ReflectiveOperationException {
        List<Backup> backupList = new ArrayList<>();

        TestHelper.setUpServer();
        TestHelper.setupManager(this.manager);

        this.loadManager(mock(StorageManager.class), mock(BackupsData.class), backupList);

        this.manager.createBackup("backup", "Utarwyn", result -> {
        });

        verify(Bukkit.getServer().getScheduler())
                .runTaskAsynchronously(any(), any(BackupCreateTask.class));
    }

    @Test
    public void applyBackup() throws ReflectiveOperationException {
        List<Backup> backupList = new ArrayList<>();

        TestHelper.setUpServer();
        TestHelper.setupManager(this.manager);

        this.loadManager(mock(StorageManager.class), mock(BackupsData.class), backupList);

        // Unknown backup
        this.manager.applyBackup("unknown", result -> assertThat(result).isFalse());

        // Registered backup
        Backup backup = mock(Backup.class);
        when(backup.getName()).thenReturn("backup");
        backupList.add(backup);

        this.manager.applyBackup("backup", result -> {
        });
        verify(Bukkit.getServer().getScheduler())
                .runTaskAsynchronously(any(), any(BackupApplyTask.class));
    }

    @Test
    public void removeBackup() throws ReflectiveOperationException {
        List<Backup> backupList = new ArrayList<>();

        TestHelper.setUpServer();
        TestHelper.setupManager(this.manager);

        this.loadManager(mock(StorageManager.class), mock(BackupsData.class), backupList);

        // Unknown backup
        this.manager.removeBackup("unknown", result -> assertThat(result).isFalse());

        // Registered backup
        Backup backup = mock(Backup.class);
        when(backup.getName()).thenReturn("backup");
        backupList.add(backup);

        this.manager.removeBackup("backup", result -> {
        });
        verify(Bukkit.getServer().getScheduler())
                .runTaskAsynchronously(any(), any(BackupRemoveTask.class));
    }

    /**
     * Load the backup manager with mocks and a backup list.
     *
     * @param storageManager mocked storage manager
     * @param storage        mocked storage object
     * @param backupList     fake backup list where backups will be saved
     * @throws ReflectiveOperationException thrown if the manager cannot be registered
     */
    private void loadManager(StorageManager storageManager, BackupsData storage,
                             List<Backup> backupList) throws ReflectiveOperationException {
        TestHelper.registerManagers(storageManager);
        when(storageManager.createBackupDataStorage()).thenReturn(storage);
        when(storage.getCachedBackups()).thenReturn(backupList);

        this.manager.load();
    }

}

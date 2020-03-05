package fr.utarwyn.endercontainers.backup.action;

import fr.utarwyn.endercontainers.EnderContainers;
import fr.utarwyn.endercontainers.TestHelper;
import fr.utarwyn.endercontainers.backup.Backup;
import fr.utarwyn.endercontainers.backup.BackupManager;
import fr.utarwyn.endercontainers.enderchest.EnderChestManager;
import fr.utarwyn.endercontainers.storage.backups.BackupsData;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class BackupApplyTaskTest {

    private EnderChestManager enderChestManager;

    @Before
    public void setUp() throws ReflectiveOperationException {
        this.enderChestManager = mock(EnderChestManager.class);

        TestHelper.setUpServer();
        TestHelper.registerManagers(this.enderChestManager);
    }

    @Test
    public void run() {
        EnderContainers plugin = TestHelper.getPlugin();
        BackupManager manager = mock(BackupManager.class);
        Backup backup = mock(Backup.class);
        BackupsData storage = mock(BackupsData.class);

        when(manager.getStorage()).thenReturn(storage);

        // Check a valid task
        BackupApplyTask task1 = new BackupApplyTask(plugin, manager, backup,
                result -> assertThat(result).isTrue());

        when(storage.applyBackup(backup)).thenReturn(true);
        task1.run();
        verify(this.enderChestManager).load();

        // Check an unvalid task
        BackupApplyTask task2 = new BackupApplyTask(plugin, manager, backup,
                result -> assertThat(result).isFalse());

        when(storage.applyBackup(backup)).thenReturn(false);
        task2.run();
        verify(this.enderChestManager, times(2)).load();
    }

}

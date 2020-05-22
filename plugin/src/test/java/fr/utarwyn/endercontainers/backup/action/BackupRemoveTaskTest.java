package fr.utarwyn.endercontainers.backup.action;

import fr.utarwyn.endercontainers.EnderContainers;
import fr.utarwyn.endercontainers.TestHelper;
import fr.utarwyn.endercontainers.backup.Backup;
import fr.utarwyn.endercontainers.backup.BackupManager;
import fr.utarwyn.endercontainers.storage.backups.BackupsData;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class BackupRemoveTaskTest {

    @Test
    public void run() throws ReflectiveOperationException {
        EnderContainers plugin = TestHelper.getPlugin();
        BackupManager manager = mock(BackupManager.class);
        Backup backup = mock(Backup.class);
        BackupsData storage = mock(BackupsData.class);
        List<Backup> backupList = new ArrayList<>();

        when(manager.getStorage()).thenReturn(storage);
        when(manager.getBackups()).thenReturn(backupList);

        // Check a valid task
        backupList.add(backup);
        when(storage.removeBackup(backup)).thenReturn(true);

        new BackupRemoveTask(plugin, manager, backup,
                result -> assertThat(result).isTrue()).run();

        verify(storage).removeBackup(backup);
        assertThat(manager.getBackups()).isEmpty();

        // Try to remove a non-exisiting backup
        when(storage.removeBackup(backup)).thenReturn(false);

        new BackupRemoveTask(plugin, manager, backup,
                result -> assertThat(result).isFalse()).run();
    }

}

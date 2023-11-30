package fr.utarwyn.endercontainers.backup.action;

import fr.utarwyn.endercontainers.EnderContainers;
import fr.utarwyn.endercontainers.TestHelper;
import fr.utarwyn.endercontainers.TestInitializationException;
import fr.utarwyn.endercontainers.backup.BackupManager;
import fr.utarwyn.endercontainers.storage.backups.BackupsData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BackupCreateTaskTest {

    private static final String NAME = "TestBackup";

    private static final String OPERATOR = "Utarwyn";

    private EnderContainers plugin;

    private BackupManager manager;

    private BackupsData storage;

    @BeforeEach
    public void setUp() throws TestInitializationException {
        TestHelper.setUpServer();

        this.plugin = TestHelper.getPlugin();
        this.manager = mock(BackupManager.class);
        this.storage = mock(BackupsData.class);

        when(manager.getStorage()).thenReturn(storage);
        when(manager.getBackups()).thenReturn(new ArrayList<>());
    }

    @Test
    public void valid() {
        when(storage.saveNewBackup(any())).thenReturn(true);
        when(storage.executeStorage(any())).thenReturn(true);

        new BackupCreateTask(plugin, manager, OPERATOR, NAME,
                result -> assertThat(result).isTrue()).run();

        verify(storage).saveNewBackup(any());
        verify(storage).executeStorage(any());
        assertThat(manager.getBackups()).hasSize(1);
    }

    @Test
    public void withStorageError() {
        // Cannot save the backup
        when(storage.saveNewBackup(any())).thenReturn(false);
        when(storage.executeStorage(any())).thenReturn(true);

        new BackupCreateTask(plugin, manager, OPERATOR, NAME,
                result -> assertThat(result).isFalse()).run();

        // Cannot execute the backup
        when(storage.saveNewBackup(any())).thenReturn(true);
        when(storage.executeStorage(any())).thenReturn(false);

        new BackupCreateTask(plugin, manager, OPERATOR, NAME,
                result -> assertThat(result).isFalse()).run();

        // No backup created at the end
        assertThat(manager.getBackups()).isEmpty();
    }

}

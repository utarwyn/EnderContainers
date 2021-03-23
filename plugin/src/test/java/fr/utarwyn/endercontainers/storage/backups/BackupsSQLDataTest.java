package fr.utarwyn.endercontainers.storage.backups;

import fr.utarwyn.endercontainers.TestHelper;
import fr.utarwyn.endercontainers.TestInitializationException;
import fr.utarwyn.endercontainers.backup.Backup;
import fr.utarwyn.endercontainers.database.DatabaseManager;
import fr.utarwyn.endercontainers.database.DatabaseSet;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Date;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class BackupsSQLDataTest {

    private static final Timestamp CURRENT = new Timestamp(new Date().getTime());

    private static final String NAME = "name";

    private static final String AUTHOR = "author";

    private static final String DATA = "1:1::Q09OVEVOVFM=:3;2:2:VXRhcnd5bg==::6";

    private BackupsSQLData data;

    @Mock
    private DatabaseManager databaseManager;

    @Mock
    private Backup backup;

    private static DatabaseSet generateBackupSet(String name, String createdBy, String data) {
        DatabaseSet set = new DatabaseSet();
        set.setObject("name", name);
        set.setObject("date", CURRENT);
        set.setObject("created_by", createdBy);
        set.setObject("data", data);
        return set;
    }

    private static DatabaseSet generateEnderchestSet(int id, int num, String owner, String contents, int rows) {
        DatabaseSet set = new DatabaseSet();
        set.setObject("id", id);
        set.setObject("num", num);
        set.setObject("owner", owner);
        set.setObject("contents", contents);
        set.setObject("rows", rows);
        return set;
    }

    @Before
    public void setUp() throws TestInitializationException {
        when(this.backup.getName()).thenReturn(NAME);
        when(this.backup.getDate()).thenReturn(CURRENT);
        when(this.backup.getCreatedBy()).thenReturn(AUTHOR);

        TestHelper.registerManagers(this.databaseManager);
        this.data = new BackupsSQLData(TestHelper.getPlugin());
    }

    @Test
    public void load() throws SQLException {
        when(this.databaseManager.getBackups()).thenReturn(Arrays.asList(
                generateBackupSet(NAME + '1', AUTHOR + '1', ""),
                generateBackupSet(NAME + '2', AUTHOR + '2', "")
        ));

        this.data.load();
        this.data.save();

        assertThat(this.data.getCachedBackups()).isNotEmpty().hasSize(2);
        assertThat(this.data.backups.get(0).getName()).isEqualTo(NAME + '1');
        assertThat(this.data.backups.get(0).getDate()).isEqualTo(CURRENT);
        assertThat(this.data.backups.get(0).getCreatedBy()).isEqualTo(AUTHOR + '1');
        assertThat(this.data.backups.get(1).getName()).isEqualTo(NAME + '2');
        assertThat(this.data.backups.get(1).getDate()).isEqualTo(CURRENT);
        assertThat(this.data.backups.get(1).getCreatedBy()).isEqualTo(AUTHOR + '2');
    }

    @Test
    public void executeStorage() {
        assertThat(this.data.executeStorage(null)).isTrue();
    }

    @Test
    public void saveNewBackup() throws SQLException {
        // Without chest in the database
        assertThat(this.data.saveNewBackup(backup)).isTrue();
        verify(this.databaseManager).saveBackup(NAME, CURRENT.getTime(), "", AUTHOR);

        // With multiple chests
        when(this.databaseManager.getAllEnderchests()).thenReturn(Arrays.asList(
                generateEnderchestSet(1, 1, null, "CONTENTS", 3),
                generateEnderchestSet(2, 2, "Utarwyn", null, 6)
        ));

        assertThat(this.data.saveNewBackup(backup)).isTrue();
        verify(this.databaseManager).saveBackup(NAME, CURRENT.getTime(), DATA, AUTHOR);
    }

    @Test
    public void applyBackup() throws SQLException {
        // No backup found
        when(this.databaseManager.getBackup(NAME)).thenReturn(Optional.empty());
        assertThat(this.data.applyBackup(this.backup)).isFalse();

        // Backup saved in the database
        when(this.databaseManager.getBackup(NAME)).thenReturn(Optional.of(generateBackupSet(NAME, AUTHOR, DATA)));
        assertThat(this.data.applyBackup(this.backup)).isTrue();
        verify(this.databaseManager).replaceEnderchests(Arrays.asList(
                generateEnderchestSet(1, 1, null, "CONTENTS", 3),
                generateEnderchestSet(2, 2, "Utarwyn", null, 6)
        ));
    }

    @Test
    public void removeBackup() throws SQLException {
        assertThat(this.data.removeBackup(this.backup)).isFalse();

        when(this.databaseManager.removeBackup(NAME)).thenReturn(true);
        assertThat(this.data.removeBackup(this.backup)).isTrue();
    }

    @Test
    public void withSQLErrors() throws SQLException {
        // Load backup
        doThrow(SQLException.class).when(this.databaseManager).getBackups();
        this.data.load();

        // Save backup
        doThrow(SQLException.class).when(this.databaseManager).saveBackup(
                anyString(), any(Long.class), anyString(), anyString()
        );
        assertThat(this.data.saveNewBackup(backup)).isFalse();

        // Apply backup
        when(this.databaseManager.getBackup(NAME)).thenReturn(Optional.of(generateBackupSet(NAME, AUTHOR, DATA)));
        doThrow(SQLException.class).when(this.databaseManager).replaceEnderchests(anyList());
        assertThat(this.data.applyBackup(backup)).isFalse();

        doThrow(SQLException.class).when(this.databaseManager).getBackup(anyString());
        assertThat(this.data.applyBackup(backup)).isFalse();

        // Remove backup
        doThrow(SQLException.class).when(this.databaseManager).removeBackup(anyString());
        assertThat(this.data.removeBackup(backup)).isFalse();
    }

}

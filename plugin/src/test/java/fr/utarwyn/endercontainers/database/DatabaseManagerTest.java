package fr.utarwyn.endercontainers.database;

import fr.utarwyn.endercontainers.TestHelper;
import fr.utarwyn.endercontainers.TestInitializationException;
import fr.utarwyn.endercontainers.database.request.DeleteRequest;
import fr.utarwyn.endercontainers.database.request.SavingRequest;
import fr.utarwyn.endercontainers.database.request.SelectRequest;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.lang.reflect.Field;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class DatabaseManagerTest {

    private DatabaseManager databaseManager;

    @Mock
    private Database database;

    @BeforeClass
    public static void setUpClass() throws TestInitializationException {
        TestHelper.overrideConfigurationValue("mysql", true);
    }

    @AfterClass
    public static void tearDown() throws TestInitializationException {
        TestHelper.overrideConfigurationValue("mysql", false);
    }

    @Before
    public void setUp() throws TestInitializationException, ReflectiveOperationException {
        this.databaseManager = new DatabaseManager();
        TestHelper.setupManager(this.databaseManager);

        // Call real methods
        lenient().when(this.database.select(any(String[].class))).thenCallRealMethod();
        lenient().when(this.database.update(anyString())).thenCallRealMethod();
        lenient().when(this.database.delete(any(String[].class))).thenCallRealMethod();

        // Force custom database
        Field field = this.databaseManager.getClass().getDeclaredField("database");
        field.setAccessible(true);
        field.set(this.databaseManager, this.database);
        field.setAccessible(false);
    }

    @Test
    public void load() {
        try {
            this.databaseManager.load();
            fail("load must fail because of unknown driver");
        } catch (Exception e) {
            assertThat(e.getCause()).isInstanceOf(SQLException.class)
                    .hasMessage("No suitable driver");
        }
    }

    @Test
    public void unload() {
        this.databaseManager.unload();
        verify(this.database).close();
    }

    @Test
    public void ready() {
        when(this.database.isRunning()).thenReturn(true);
        assertThat(this.databaseManager.isReady()).isTrue();
        when(this.database.isRunning()).thenReturn(false);
        assertThat(this.databaseManager.isReady()).isFalse();
    }

    @Test
    public void saveEnderchestInsert() throws SQLException {
        UUID uuid = UUID.randomUUID();
        ArgumentCaptor<SavingRequest> request = ArgumentCaptor.forClass(SavingRequest.class);

        this.databaseManager.saveEnderchest(true, uuid, 2, 6, "data");
        verify(this.database).execUpdateStatement(request.capture());
        assertThat(request.getValue().getRequest()).startsWith("INSERT INTO");
        assertThat(request.getValue().getAttributes()).hasSameElementsAs(Arrays.asList(2, uuid.toString(), 6, "data"));
    }

    @Test
    public void saveEnderchestUpdate() throws SQLException {
        UUID uuid = UUID.randomUUID();
        ArgumentCaptor<SavingRequest> request = ArgumentCaptor.forClass(SavingRequest.class);

        this.databaseManager.saveEnderchest(false, uuid, 3, 5, "data2");
        verify(this.database).execUpdateStatement(request.capture());
        assertThat(request.getValue().getRequest()).startsWith("UPDATE");
        assertThat(request.getValue().getAttributes()).hasSameElementsAs(Arrays.asList(3, uuid.toString(), 5, "data2"));
    }

    @Test
    public void getAllEnderchests() throws SQLException {
        ArgumentCaptor<SelectRequest> request = ArgumentCaptor.forClass(SelectRequest.class);
        when(this.database.execQueryStatement(request.capture())).thenReturn(Collections.singletonList(new DatabaseSet()));
        assertThat(this.databaseManager.getAllEnderchests()).isNotEmpty().hasSize(1);
        assertThat(request.getValue().getRequest()).contains("enderchests");
    }

    @Test
    public void getEnderchestsOf() throws SQLException {
        UUID uuid = UUID.randomUUID();
        ArgumentCaptor<SelectRequest> request = ArgumentCaptor.forClass(SelectRequest.class);

        when(this.database.execQueryStatement(request.capture())).thenReturn(Collections.singletonList(new DatabaseSet()));
        assertThat(this.databaseManager.getEnderchestsOf(uuid)).isNotEmpty().hasSize(1);
        assertThat(request.getValue().getAttributes()).hasSameElementsAs(Collections.singletonList(uuid.toString()));
    }

    @Test
    public void replaceEnderchests() throws SQLException {
        List<DatabaseSet> sets = Arrays.asList(new DatabaseSet(), new DatabaseSet());
        this.databaseManager.replaceEnderchests(sets);
        verify(this.database).execUpdateStatement(any(DeleteRequest.class));
        verify(this.database, times(2)).execUpdateStatement(any(SavingRequest.class));
    }

    @Test
    public void getBackups() throws SQLException {
        when(this.database.execQueryStatement(any(SelectRequest.class)))
                .thenReturn(Arrays.asList(new DatabaseSet(), new DatabaseSet()));

        assertThat(this.databaseManager.getBackups()).isNotEmpty().hasSize(2);
    }

    @Test
    public void getBackup() throws SQLException {
        ArgumentCaptor<SelectRequest> request = ArgumentCaptor.forClass(SelectRequest.class);

        when(this.database.execQueryStatement(request.capture())).thenReturn(Collections.singletonList(new DatabaseSet()));
        assertThat(this.databaseManager.getBackup("name")).isNotEmpty();
        assertThat(request.getValue().getAttributes()).hasSameElementsAs(Collections.singletonList("name"));
    }

    @Test
    public void saveBackup() throws SQLException {
        ArgumentCaptor<SavingRequest> request = ArgumentCaptor.forClass(SavingRequest.class);

        this.databaseManager.saveBackup("name", 0L, "data", "Utarwyn");
        verify(this.database).execUpdateStatement(request.capture());
        assertThat(request.getValue().getRequest()).startsWith("INSERT INTO");
        assertThat(request.getValue().getAttributes()).hasSameElementsAs(Arrays.asList("name", new Timestamp(0L), "data", "Utarwyn"));
    }

    @Test
    public void removeExisitingBackup() throws SQLException {
        ArgumentCaptor<DeleteRequest> request = ArgumentCaptor.forClass(DeleteRequest.class);

        DatabaseSet set = new DatabaseSet();
        set.setObject("id", 2);

        when(this.database.execQueryStatement(any(SelectRequest.class)))
                .thenReturn(Collections.singletonList(set));
        when(this.database.execUpdateStatement(request.capture())).thenReturn(true);

        assertThat(this.databaseManager.removeBackup("name")).isTrue();
        assertThat(request.getValue().getAttributes()).hasSameElementsAs(Collections.singletonList(2));
    }

    @Test
    public void removeUnknownBackup() throws SQLException {
        assertThat(this.databaseManager.removeBackup("name")).isFalse();
    }

}

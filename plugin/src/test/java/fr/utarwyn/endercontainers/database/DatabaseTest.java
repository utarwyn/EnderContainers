package fr.utarwyn.endercontainers.database;

import com.zaxxer.hikari.HikariDataSource;
import fr.utarwyn.endercontainers.TestHelper;
import fr.utarwyn.endercontainers.configuration.wrapper.YamlFileLoadException;
import fr.utarwyn.endercontainers.database.adapter.MySQLAdapter;
import fr.utarwyn.endercontainers.database.request.DeleteRequest;
import fr.utarwyn.endercontainers.database.request.SavingRequest;
import fr.utarwyn.endercontainers.database.request.SelectRequest;
import org.bukkit.configuration.InvalidConfigurationException;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;
import java.sql.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class DatabaseTest {

    @Mock(answer = Answers.CALLS_REAL_METHODS)
    private Database database;

    @BeforeClass
    public static void setUpClass() throws InvalidConfigurationException,
            ReflectiveOperationException, IOException, YamlFileLoadException {
        TestHelper.setUpFiles();
    }

    @Before
    public void setUp() {
        this.database.source = mock(HikariDataSource.class);
    }

    @Test
    public void serverUrl() {
        Database mySQLDatabase = new Database(new MySQLAdapter(), null);
        assertThat(mySQLDatabase.getServerUrl()).isEqualTo("localhost:3306");
    }

    @Test
    public void withSecureCredentials() {
        DatabaseSecureCredentials credentials = new DatabaseSecureCredentials();
        credentials.setClientKeystore("client.p12", "changeit");
        credentials.setTrustKeystore("trust.p12", "changeit");
        Database secureDatabase = new Database(new MySQLAdapter(), credentials);

        assertThat(secureDatabase.isSecure()).isTrue();
    }

    @Test
    public void initializationErrors() {
        // With a MySQL adapter
        Database mySQLDatabase = new Database(new MySQLAdapter(), null);
        assertThatExceptionOfType(RuntimeException.class)
                .isThrownBy(mySQLDatabase::initialize)
                .withCauseInstanceOf(SQLException.class)
                .withMessage("Failed to get driver instance for " +
                        "jdbcUrl=jdbc:mysql://localhost:3306/database");
    }

    @Test
    public void isRunning() {
        // Not connected if the connection pool is not running
        when(this.database.source.isRunning()).thenReturn(false);
        assertThat(this.database.isRunning()).isFalse();

        // Running if the connection pool is running
        when(this.database.source.isRunning()).thenReturn(true);
        assertThat(this.database.isRunning()).isTrue();
    }

    @Test
    public void close() {
        // Can call the close method without a valid opened connection
        this.database.close();

        // Call the data source close method
        when(this.database.isRunning()).thenReturn(true);
        this.database.close();
        verify(this.database.source).close();
    }

    @Test
    public void getServerVersion() throws SQLException {
        Connection connection = mock(Connection.class);
        DatabaseMetaData metaData = mock(DatabaseMetaData.class);

        when(connection.getMetaData()).thenReturn(metaData);
        when(this.database.source.getConnection()).thenReturn(connection);

        when(metaData.getDatabaseProductVersion()).thenReturn("5.5.8-MySQL");
        assertThat(this.database.getServerVersion()).isEqualTo(5.5);
        when(metaData.getDatabaseProductVersion()).thenReturn("4.9.2");
        assertThat(this.database.getServerVersion()).isEqualTo(4.9);
    }

    @Test
    public void getTables() throws SQLException {
        Connection connection = mock(Connection.class);
        DatabaseMetaData metaData = mock(DatabaseMetaData.class);
        ResultSet resultSet = mock(ResultSet.class);

        when(resultSet.next()).thenReturn(true).thenReturn(true).thenReturn(false);
        when(resultSet.getString(3)).thenReturn("table1").thenReturn("table2");

        when(metaData.getTables(null, null, "%", null)).thenReturn(resultSet);
        when(connection.getMetaData()).thenReturn(metaData);
        when(this.database.source.getConnection()).thenReturn(connection);

        assertThat(this.database.getTables()).containsExactlyInAnyOrder("table1", "table2");
    }

    @Test
    public void performSelect() throws SQLException {
        PreparedStatement statement = this.createFakeStatement();

        SelectRequest request = this.database.select("any", "field", "here")
                .from("table1").where("id = ?").attributes(1);
        request.find();

        verify(this.database, times(1)).execQueryStatement(request);
        verify(statement, times(1)).executeQuery();
    }

    @Test
    public void performUpdate() throws SQLException {
        PreparedStatement statement = this.createFakeStatement();

        when(statement.executeUpdate()).thenReturn(1);

        SavingRequest request = this.database.update("table1")
                .fields("field").values("test");
        assertThat(request.execute()).isTrue();

        verify(this.database, times(1)).execUpdateStatement(request);
        verify(statement, times(1)).executeUpdate();
    }

    @Test
    public void performDelete() throws SQLException {
        PreparedStatement statement = this.createFakeStatement();

        when(statement.executeUpdate()).thenReturn(1);

        DeleteRequest request = this.database.delete("field1 = ?")
                .from("table2").attributes("test");
        assertThat(request.execute()).isTrue();

        verify(this.database, times(1)).execUpdateStatement(request);
        verify(statement, times(1)).executeUpdate();
    }

    @Test
    public void performCustomRequest() throws SQLException {
        Statement statement = mock(Statement.class);
        String request = "SELECT * FROM dual";

        when(this.database.source.getConnection()).thenReturn(mock(Connection.class));
        when(this.database.source.getConnection().createStatement()).thenReturn(statement);
        this.database.request(request);
        verify(statement).executeUpdate(request);
    }

    private PreparedStatement createFakeStatement() throws SQLException {
        PreparedStatement statement = mock(PreparedStatement.class);
        Connection connection = mock(Connection.class);
        ResultSet resultSet = mock(ResultSet.class);
        ResultSetMetaData resultSetMetaData = mock(ResultSetMetaData.class);

        when(resultSetMetaData.getColumnCount()).thenReturn(0);
        when(resultSet.getMetaData()).thenReturn(resultSetMetaData);
        when(statement.executeQuery()).thenReturn(resultSet);
        when(connection.prepareStatement(anyString())).thenReturn(statement);
        when(this.database.source.getConnection()).thenReturn(connection);

        return statement;
    }

}

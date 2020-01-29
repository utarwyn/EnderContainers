package fr.utarwyn.endercontainers.database;

import org.apache.commons.dbcp2.BasicDataSource;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class DatabaseTest {

    private Database database;

    @Before
    public void setUp() {
        this.database = new Database(
                "localhost", 3306,
                "myusername", "mypassword", "endercontainers"
        );
    }

    @Test
    public void openConnection() throws SQLException {
        assertThat(this.database.getEndpoint()).isEqualTo("localhost:3306");

        // The JDBC driver is not loaded in the test environment
        assertThatExceptionOfType(SQLException.class)
                .isThrownBy(() -> this.database.open())
                .withMessage("Cannot load JDBC driver class 'com.mysql.jdbc.Driver'");

        // Mock the connection and try
        BasicDataSource dataSource = this.mockConnection();
        this.database.open();

        verify(dataSource).setDriverClassName("com.mysql.jdbc.Driver");
        verify(dataSource).setUrl(anyString());
        verify(dataSource).setUsername("myusername");
        verify(dataSource).setPassword("mypassword");
        verify(dataSource.getConnection()).close();
    }

    @Test
    public void openASecuredConnection() throws SQLException {
        assertThat(this.database.isSecured()).isFalse();

        BasicDataSource dataSource = this.mockConnection();
        this.setUpSecureCredentials();
        this.database.open();

        assertThat(this.database.isSecured()).isTrue();
        verify(dataSource).addConnectionProperty("useSSL", "true");
        verify(dataSource).addConnectionProperty("requireSSL", "true");
        verify(dataSource).addConnectionProperty("clientCertificateKeyStorePassword", "changeit");
        verify(dataSource).addConnectionProperty("clientCertificateKeyStoreType", "PKCS12");
    }

    @Test
    public void isConnected() throws SQLException {
        // Default state is not connected
        assertThat(this.database.isConnected()).isFalse();

        // Not connected after a connection error
        try {
            this.database.open();
            fail("database connection should not be opened");
        } catch (SQLException e) {
            assertThat(this.database.isConnected()).isFalse();
        }

        // Connected after a successful connection
        this.mockConnection();
        this.database.open();
        assertThat(this.database.isConnected()).isTrue();
    }

    @Test
    public void closeConnection() throws SQLException {
        // Can call the close method without a valid opened connection
        this.database.close();

        // Call the data source close method
        BasicDataSource dataSource = this.mockConnection();
        this.database.open();
        this.database.close();
        verify(dataSource).close();
    }

    @Test
    public void performRequest() throws SQLException {
        BasicDataSource dataSource = this.mockConnection();
        Statement statement = mock(Statement.class);
        String request = "SELECT * FROM test";

        when(dataSource.getConnection().createStatement()).thenReturn(statement);
        this.database.request(request);
        verify(statement).executeUpdate(request);
    }

    /**
     * Setup credentials to simulate a secure connection over SSL.
     */
    private void setUpSecureCredentials() {
        DatabaseSecureCredentials credentials = new DatabaseSecureCredentials();
        credentials.setClientKeystore("client.p12", "changeit");
        credentials.setTrustKeystore("ca.p12", "changeit");
        this.database.setSecureCredentials(credentials);
    }

    /**
     * Generates a mock of the data source used to connect to a database.
     * Also put this object inside the database test instance.
     *
     * @return the generated mock
     * @throws SQLException thrown if the datasource cannot be instantiated
     */
    private BasicDataSource mockConnection() throws SQLException {
        BasicDataSource dataSource = mock(BasicDataSource.class);
        Connection connection = mock(Connection.class);

        when(dataSource.getConnection()).thenReturn(connection);
        when(dataSource.isClosed()).thenReturn(false);

        this.database.source = dataSource;
        return dataSource;
    }

}

package fr.utarwyn.endercontainers.database;

import com.zaxxer.hikari.HikariConfig;
import fr.utarwyn.endercontainers.configuration.Configuration;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DatabaseSecureCredentialsTest {

    private static final String DEFAULT_FILE = "test.p12";

    private static final String DEFAULT_PASSWORD = "changeit";

    private HikariConfig poolConfig;

    private DatabaseSecureCredentials credentials;

    @Before
    public void setUp() {
        this.credentials = new DatabaseSecureCredentials();
        this.poolConfig = new HikariConfig();
    }

    @Test
    public void withClientKeystore() {
        // Valid client keystore parameters
        this.credentials.setClientKeystore(DEFAULT_FILE, DEFAULT_PASSWORD);

        this.credentials.apply(this.poolConfig);
        Properties properties = this.poolConfig.getDataSourceProperties();

        assertThat(properties.getProperty("clientCertificateKeyStoreUrl"))
                .isNotNull().endsWith(DEFAULT_FILE);
        assertThat(properties.getProperty("clientCertificateKeyStorePassword"))
                .isNotNull().isEqualTo(DEFAULT_PASSWORD);
        assertThat(properties.getProperty("trustCertificateKeyStoreUrl")).isNull();

        // Unvalid client keystore parameters
        assertThatNullPointerException()
                .isThrownBy(() -> this.credentials.setClientKeystore(null, DEFAULT_PASSWORD))
                .withNoCause();
        assertThatNullPointerException()
                .isThrownBy(() -> this.credentials.setClientKeystore(DEFAULT_FILE, null))
                .withNoCause();
        assertThatNullPointerException()
                .isThrownBy(() -> this.credentials.setClientKeystore(null, null))
                .withNoCause();
    }

    @Test
    public void withTrustKeystore() {
        // Also put the client keystore first
        this.credentials.setClientKeystore(DEFAULT_FILE, DEFAULT_PASSWORD);

        // Valid trust keystore parameters
        this.credentials.setTrustKeystore(DEFAULT_FILE, DEFAULT_PASSWORD);

        this.credentials.apply(this.poolConfig);
        Properties properties = this.poolConfig.getDataSourceProperties();

        assertThat(properties.getProperty("trustCertificateKeyStoreUrl"))
                .isNotNull().endsWith(DEFAULT_FILE);
        assertThat(properties.getProperty("trustCertificateKeyStorePassword"))
                .isNotNull().isEqualTo(DEFAULT_PASSWORD);

        // Unvalid trust keystore parameters
        assertThatNullPointerException()
                .isThrownBy(() -> this.credentials.setTrustKeystore(null, DEFAULT_PASSWORD))
                .withNoCause();

        assertThatNullPointerException()
                .isThrownBy(() -> this.credentials.setTrustKeystore(DEFAULT_FILE, null))
                .withNoCause();
    }

    @Test
    public void fromConfig() {
        // From an empty config
        Configuration configuration = mock(Configuration.class);
        when(configuration.isMysqlSsl()).thenReturn(false);
        assertThat(DatabaseSecureCredentials.fromConfig(configuration)).isEmpty();

        // From a configured configuration
        when(configuration.isMysqlSsl()).thenReturn(true);
        when(configuration.getMysqlSslKeystoreFile()).thenReturn("client.p12");
        when(configuration.getMysqlSslKeystorePassword()).thenReturn("changeit");
        when(configuration.getMysqlSslTrustKeystoreFile()).thenReturn("trust.p12");
        when(configuration.getMysqlSslTrustKeystorePassword()).thenReturn("changeit");
        assertThat(DatabaseSecureCredentials.fromConfig(configuration)).isNotEmpty();
    }

}

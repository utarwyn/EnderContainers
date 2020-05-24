package fr.utarwyn.endercontainers.database;

import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThatNullPointerException;

public class DatabaseSecureCredentialsTest {

    private static final String DEFAULT_FILE = "test.p12";

    private static final String DEFAULT_PASSWORD = "changeit";

    private DatabaseSecureCredentials credentials;

    @Before
    public void setUp() {
        this.credentials = new DatabaseSecureCredentials();
    }

    @Test
    public void withClientKeystore() {
        /*// Valid client keystore parameters
        this.credentials.setClientKeystore(DEFAULT_FILE, DEFAULT_PASSWORD);

        assertThat(this.credentials.getClientKeystoreFile())
                .isNotNull().endsWith(DEFAULT_FILE);
        assertThat(this.credentials.getClientKeystorePassword())
                .isNotNull().isEqualTo(DEFAULT_PASSWORD);
        assertThat(this.credentials.isUsingTrustCertificate()).isFalse();*/

        // Unvalid client keystore parameters
        assertThatNullPointerException()
                .isThrownBy(() -> this.credentials.setClientKeystore(null, DEFAULT_PASSWORD))
                .withNoCause();

        assertThatNullPointerException()
                .isThrownBy(() -> this.credentials.setClientKeystore(DEFAULT_FILE, null))
                .withNoCause();
    }

    @Test
    public void withTrustKeystore() {
        /*// Valid trust keystore parameters
        this.credentials.setTrustKeystore(DEFAULT_FILE, DEFAULT_PASSWORD);
        assertThat(this.credentials.getTrustKeystoreFile())
                .isNotNull().endsWith(DEFAULT_FILE);
        assertThat(this.credentials.getTrustKeystorePassword())
                .isNotNull().isEqualTo(DEFAULT_PASSWORD);
        assertThat(this.credentials.isUsingTrustCertificate()).isTrue();*/

        // Unvalid trust keystore parameters
        assertThatNullPointerException()
                .isThrownBy(() -> this.credentials.setTrustKeystore(null, DEFAULT_PASSWORD))
                .withNoCause();

        assertThatNullPointerException()
                .isThrownBy(() -> this.credentials.setTrustKeystore(DEFAULT_FILE, null))
                .withNoCause();
    }

}

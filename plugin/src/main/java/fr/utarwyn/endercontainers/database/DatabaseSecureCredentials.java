package fr.utarwyn.endercontainers.database;

import com.zaxxer.hikari.HikariConfig;
import fr.utarwyn.endercontainers.configuration.Configuration;

import java.io.File;
import java.util.Objects;
import java.util.Optional;

/**
 * Stores credentials to open a
 * connection to a database server over SSL.
 *
 * @author Utarwyn
 * @since 2.2.0
 */
public class DatabaseSecureCredentials {

    /**
     * Type of keystore to be used for certificates.
     * See https://en.wikipedia.org/wiki/PKCS_12 for more info.
     */
    public static final String KEYSTORE_TYPE = "PKCS12";

    /**
     * Absolute path to the client certificate keystore file.
     */
    private String clientKeystoreFile;

    /**
     * Password to open the client certificate keystore.
     */
    private String clientKeystorePassword;

    /**
     * Absolute path to the trust certificate keystore kile.
     */
    private String trustKeystoreFile;

    /**
     * Password to open thee trust certificate keystore.
     */
    private String trustKeystorePassword;

    /**
     * Creates a new database secure credentials
     * object from the plugin configuration file.
     *
     * @param config plugin configuration object
     * @return generated credentials object, empty if SSL disabled in the config
     */
    public static Optional<DatabaseSecureCredentials> fromConfig(Configuration config) {
        if (!config.isMysqlSsl()) return Optional.empty();
        DatabaseSecureCredentials instance = new DatabaseSecureCredentials();

        instance.setClientKeystore(
                config.getMysqlSslKeystoreFile(),
                config.getMysqlSslKeystorePassword()
        );

        if (config.getMysqlSslTrustKeystoreFile() != null
                && config.getMysqlSslTrustKeystorePassword() != null) {
            instance.setTrustKeystore(
                    config.getMysqlSslTrustKeystoreFile(),
                    config.getMysqlSslTrustKeystorePassword()
            );
        }

        return Optional.of(instance);
    }

    /**
     * Registers a client keystore for the configuration.
     *
     * @param file     client keystore file path
     * @param password client keystore password
     */
    public void setClientKeystore(String file, String password) {
        this.clientKeystoreFile = new File(file).toURI().toString();
        this.clientKeystorePassword = Objects.requireNonNull(password);
    }

    /**
     * Registers a trust keystore for the configuration.
     *
     * @param file     trust keystore file path
     * @param password trust keystore password
     */
    public void setTrustKeystore(String file, String password) {
        this.trustKeystoreFile = new File(file).toURI().toString();
        this.trustKeystorePassword = Objects.requireNonNull(password);
    }

    /**
     * Apply the SSL configuration on a data source configuration object.
     *
     * @param config source configuration object
     */
    public void apply(HikariConfig config) {
        config.addDataSourceProperty("useSSL", "true");
        config.addDataSourceProperty("requireSSL", "true");

        config.addDataSourceProperty("clientCertificateKeyStoreUrl",
                this.clientKeystoreFile);
        config.addDataSourceProperty("clientCertificateKeyStoreType",
                DatabaseSecureCredentials.KEYSTORE_TYPE);
        config.addDataSourceProperty("clientCertificateKeyStorePassword",
                this.clientKeystorePassword);

        if (this.trustKeystoreFile != null && this.trustKeystorePassword != null) {
            config.addDataSourceProperty("trustCertificateKeyStoreUrl",
                    this.trustKeystoreFile);
            config.addDataSourceProperty("trustCertificateKeyStoreType",
                    DatabaseSecureCredentials.KEYSTORE_TYPE);
            config.addDataSourceProperty("trustCertificateKeyStorePassword",
                    this.trustKeystorePassword);
        }
    }

}

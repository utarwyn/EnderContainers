package fr.utarwyn.endercontainers.database;

import java.io.File;
import java.util.Objects;

/**
 * Stores credentials to open a connection to a database server over SSL.
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

    public String getClientKeystoreFile() {
        return this.clientKeystoreFile;
    }

    public String getClientKeystorePassword() {
        return this.clientKeystorePassword;
    }

    public String getTrustKeystoreFile() {
        return this.trustKeystoreFile;
    }

    public String getTrustKeystorePassword() {
        return this.trustKeystorePassword;
    }

    public boolean isUsingTrustCertificate() {
        return this.trustKeystoreFile != null && this.trustKeystorePassword != null;
    }

    public void setClientKeystore(String file, String password) {
        this.clientKeystoreFile = new File(file).toURI().toString();
        this.clientKeystorePassword = Objects.requireNonNull(password);
    }

    public void setTrustKeystore(String file, String password) {
        this.trustKeystoreFile = new File(file).toURI().toString();
        this.trustKeystorePassword = Objects.requireNonNull(password);
    }

}

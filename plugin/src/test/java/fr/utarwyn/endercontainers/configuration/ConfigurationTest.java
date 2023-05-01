package fr.utarwyn.endercontainers.configuration;

import fr.utarwyn.endercontainers.TestHelper;
import fr.utarwyn.endercontainers.TestInitializationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

@RunWith(MockitoJUnitRunner.class)
public class ConfigurationTest {

    @BeforeClass
    public static void setUpClass() throws TestInitializationException {
        TestHelper.setUpFiles();
    }

    @Test
    public void loadFromTestFile() throws ConfigLoadingException, TestInitializationException {
        Configuration config = new Configuration(TestHelper.getPlugin().getConfig());
        assertThat(config.getLocale()).isEqualTo("en");
        assertThat(config.getDisabledWorlds()).containsExactly("disabled");
        assertThat(config.isOnlyShowAccessibleEnderchests()).isFalse();
        assertThat(config.isUseVanillaEnderchest()).isTrue();
        assertThat(config.isNumberingEnderchests()).isTrue();
        assertThat(config.getMysqlSslKeystoreFile()).isNull();
        assertThat(config.getMysqlSslKeystorePassword()).isNull();
        assertThat(config.getMysqlSslTrustKeystoreFile()).isNull();
        assertThat(config.getMysqlSslTrustKeystorePassword()).isNull();
    }

    @Test
    public void loadDatabaseSSLValues() throws ConfigLoadingException, TestInitializationException {
        FileConfiguration fileConfiguration = new YamlConfiguration();
        fileConfiguration.setDefaults(TestHelper.getPlugin().getConfig());
        fileConfiguration.set("mysql.ssl.enabled", true);
        fileConfiguration.set("mysql.ssl.keystore_file", "keystore_file");
        fileConfiguration.set("mysql.ssl.keystore_password", "keystore_password");
        fileConfiguration.set("mysql.ssl.ca_keystore_file", "ca_keystore_file");
        fileConfiguration.set("mysql.ssl.ca_keystore_password", "ca_keystore_password");

        Configuration config = new Configuration(fileConfiguration);
        assertThat(config.getMysqlSslTrustKeystorePassword()).isEqualTo("ca_keystore_password");
    }

    @Test
    public void loadValueError() {
        FileConfiguration fileConfiguration = mock(FileConfiguration.class);
        try {
            new Configuration(fileConfiguration);
            fail("configuration with a value error must fail");
        } catch (ConfigLoadingException e) {
            assertThat(e.getMessage()).endsWith("configuration");
        }
    }

}

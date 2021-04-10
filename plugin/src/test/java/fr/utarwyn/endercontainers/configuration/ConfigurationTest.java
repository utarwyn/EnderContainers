package fr.utarwyn.endercontainers.configuration;

import fr.utarwyn.endercontainers.TestHelper;
import fr.utarwyn.endercontainers.TestInitializationException;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class ConfigurationTest {

    private Configuration config;

    @BeforeClass
    public static void setUpClass() throws TestInitializationException {
        TestHelper.setUpFiles();
    }

    @Before
    public void setUp() throws TestInitializationException, ConfigLoadingException {
        this.config = new Configuration(TestHelper.getPlugin());
    }

    @Test
    public void get() {
        assertThat(this.config.isDebug()).isFalse();
        assertThat(this.config.getLocale()).isEqualTo("en");
        assertThat(this.config.getDisabledWorlds()).containsExactly("disabled");
        assertThat(this.config.getMysqlSslKeystoreFile()).isNull();
        assertThat(this.config.getMysqlSslKeystorePassword()).isNull();
        assertThat(this.config.getMysqlSslTrustKeystoreFile()).isNull();
        assertThat(this.config.getMysqlSslTrustKeystorePassword()).isNull();
    }

}

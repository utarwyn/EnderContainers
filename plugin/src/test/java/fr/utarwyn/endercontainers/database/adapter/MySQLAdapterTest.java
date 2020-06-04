package fr.utarwyn.endercontainers.database.adapter;

import com.zaxxer.hikari.HikariConfig;
import fr.utarwyn.endercontainers.configuration.Configuration;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class MySQLAdapterTest {

    private MySQLAdapter adapter;

    @Mock
    private Configuration pluginConfig;

    @Before
    public void setUp() {
        this.adapter = new MySQLAdapter();
    }

    @Test
    public void getServerUrl() {
        when(this.pluginConfig.getMysqlHost()).thenReturn("localhost");
        when(this.pluginConfig.getMysqlPort()).thenReturn(3307);

        assertThat(this.adapter.getServerUrl(this.pluginConfig))
                .isEqualTo("localhost:3307");
    }

    @Test
    public void getSourceUrl() {
        assertThat(this.adapter.getSourceUrl("localhost:3307", "database"))
                .isEqualTo("jdbc:mysql://localhost:3307/database");
    }

    @Test
    public void configure() {
        HikariConfig config = new HikariConfig();

        when(this.pluginConfig.getMysqlUser()).thenReturn("username");
        when(this.pluginConfig.getMysqlPassword()).thenReturn("password");

        this.adapter.configure(config, this.pluginConfig);

        assertThat(config.getUsername()).isEqualTo("username");
        assertThat(config.getPassword()).isEqualTo("password");
        assertThat(config.getDataSourceProperties())
                .containsEntry("useSSL", "false")
                .containsEntry("encoding", "UTF-8");
    }

}

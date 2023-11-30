package fr.utarwyn.endercontainers.database.adapter;

import com.zaxxer.hikari.HikariConfig;
import fr.utarwyn.endercontainers.configuration.Configuration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class MySQLAdapterTest {

    private MySQLAdapter adapter;

    @Mock
    private Configuration pluginConfig;

    @BeforeEach
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

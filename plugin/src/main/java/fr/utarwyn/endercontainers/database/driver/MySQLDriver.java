package fr.utarwyn.endercontainers.database.driver;

import com.zaxxer.hikari.HikariConfig;
import fr.utarwyn.endercontainers.configuration.Configuration;

/**
 * @author Utarwyn <maximemalgorn@gmail.com>
 */
public class MySQLDriver implements DatabaseDriver {

    /**
     * {@inheritDoc}
     */
    @Override
    public String getServerUrl(Configuration pluginConfig) {
        return String.format("%s:%d", pluginConfig.getMysqlHost(), pluginConfig.getMysqlPort());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getSourceUrl(String serverUrl, String databaseName) {
        return String.format("jdbc:mysql://%s/%s", serverUrl, databaseName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void configure(HikariConfig databaseConfig, Configuration pluginConfig) {
        // Auth and security
        databaseConfig.addDataSourceProperty("useSSL", "false");
        databaseConfig.setUsername(pluginConfig.getMysqlUser());
        databaseConfig.setPassword(pluginConfig.getMysqlPassword());

        // Encoding
        databaseConfig.addDataSourceProperty("characterEncoding", "utf8");
        databaseConfig.addDataSourceProperty("encoding", "UTF-8");
        databaseConfig.addDataSourceProperty("useUnicode", "true");
    }

}

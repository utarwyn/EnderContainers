package fr.utarwyn.endercontainers.database.driver;

import com.zaxxer.hikari.HikariConfig;
import fr.utarwyn.endercontainers.configuration.Configuration;

/**
 * Represents a database driver configuration object.
 * Can handle all types of SQL drivers and sources.
 *
 * @author Utarwyn <maximemalgorn@gmail.com>
 * @since 2.2.0
 */
public interface DatabaseDriver {

    String getServerUrl(Configuration pluginConfig);

    String getSourceUrl(String serverUrl, String databaseName);

    void configure(HikariConfig databaseConfig, Configuration pluginConfig);

}

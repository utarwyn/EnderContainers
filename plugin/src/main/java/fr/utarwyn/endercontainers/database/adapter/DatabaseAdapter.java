package fr.utarwyn.endercontainers.database.adapter;

import com.zaxxer.hikari.HikariConfig;
import fr.utarwyn.endercontainers.configuration.Configuration;

/**
 * Represents a database adapter.
 * Can handle all types of SQL drivers and sources.
 *
 * @author Utarwyn <maximemalgorn@gmail.com>
 * @since 2.2.0
 */
public interface DatabaseAdapter {

    /**
     * Generates the server url based on the
     * plugin configuration for the database adapter.
     *
     * @param pluginConfig plugin configuration
     * @return generated server url
     */
    String getServerUrl(Configuration pluginConfig);

    /**
     * Generates the source JDBC url from
     * the server url and the name of the database.
     *
     * @param serverUrl    server url
     * @param databaseName name of the database to connect to
     * @return generated source url
     */
    String getSourceUrl(String serverUrl, String databaseName);

    /**
     * Configures the database configuration for the source
     * with the plugin configuration.
     *
     * @param databaseConfig database configuration
     * @param pluginConfig   plugin configuration
     */
    void configure(HikariConfig databaseConfig, Configuration pluginConfig);

}

package fr.utarwyn.endercontainers.configuration;

import fr.utarwyn.endercontainers.configuration.wrapper.ConfigurableFileWrapper;
import fr.utarwyn.endercontainers.configuration.wrapper.YamlFileLoadException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.List;

/**
 * Configuration class. Reflects the config.yml
 *
 * @author Utarwyn
 * @since 2.0.0
 */
public class Configuration extends ConfigurableFileWrapper {

    /**
     * EnderContainers plugin instance
     */
    private final Plugin plugin;

    @Configurable
    private boolean debug;

    @Configurable
    private String locale;

    @Configurable
    private List<String> disabledWorlds;

    @Configurable(key = "enderchests.max")
    private int maxEnderchests;

    @Configurable(key = "enderchests.default")
    private int defaultEnderchests;

    @Configurable(key = "enderchests.onlyShowAccessible")
    private boolean onlyShowAccessibleEnderchests;

    @Configurable(key = "enderchests.useVanillaEnderchest")
    private boolean useVanillaEnderchest;

    @Configurable(key = "mysql.enabled")
    private boolean mysql;

    @Configurable(key = "mysql.host")
    private String mysqlHost;

    @Configurable(key = "mysql.port")
    private int mysqlPort;

    @Configurable(key = "mysql.user")
    private String mysqlUser;

    @Configurable(key = "mysql.password")
    private String mysqlPassword;

    @Configurable(key = "mysql.database")
    private String mysqlDatabase;

    @Configurable(key = "mysql.ssl.enabled")
    private boolean mysqlSsl;

    @Configurable(key = "mysql.ssl.keystore_file")
    private String mysqlSslKeystoreFile;

    @Configurable(key = "mysql.ssl.keystore_password")
    private String mysqlSslKeystorePassword;

    @Configurable(key = "mysql.ssl.ca_keystore_file")
    private String mysqlSslTrustKeystoreFile;

    @Configurable(key = "mysql.ssl.ca_keystore_password")
    private String mysqlSslTrustKeystorePassword;

    @Configurable(key = "mysql.tablePrefix")
    private String mysqlTablePrefix;

    @Configurable(key = "others.blockNametag")
    private boolean blockNametag;

    @Configurable(key = "others.updateChecker")
    private boolean updateChecker;

    @Configurable(key = "others.globalSound")
    private boolean globalSound;

    /**
     * Create a configuration object.
     * We don't have to provide a file object because its managed by Bukkit.
     *
     * @param plugin the Bukkit plugin
     */
    Configuration(JavaPlugin plugin) {
        super(null);
        this.plugin = plugin;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected FileConfiguration createConfiguration(File file) {
        this.plugin.saveDefaultConfig();
        return this.plugin.getConfig();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void load() throws YamlFileLoadException {
        this.plugin.reloadConfig();
        super.load();
    }

    public boolean isDebug() {
        return this.debug;
    }

    public String getLocale() {
        return this.locale;
    }

    public List<String> getDisabledWorlds() {
        return this.disabledWorlds;
    }

    public Integer getMaxEnderchests() {
        return this.maxEnderchests;
    }

    public Integer getDefaultEnderchests() {
        return this.defaultEnderchests;
    }

    public boolean isOnlyShowAccessibleEnderchests() {
        return this.onlyShowAccessibleEnderchests;
    }

    public boolean isUseVanillaEnderchest() {
        return this.useVanillaEnderchest;
    }

    public boolean isMysql() {
        return this.mysql;
    }

    public String getMysqlHost() {
        return this.mysqlHost;
    }

    public int getMysqlPort() {
        return this.mysqlPort;
    }

    public String getMysqlUser() {
        return this.mysqlUser;
    }

    public String getMysqlPassword() {
        return this.mysqlPassword;
    }

    public String getMysqlDatabase() {
        return this.mysqlDatabase;
    }

    public boolean isMysqlSsl() {
        return this.mysqlSsl;
    }

    public String getMysqlSslKeystoreFile() {
        return this.mysqlSslKeystoreFile;
    }

    public String getMysqlSslKeystorePassword() {
        return this.mysqlSslKeystorePassword;
    }

    public String getMysqlSslTrustKeystoreFile() {
        return this.mysqlSslTrustKeystoreFile;
    }

    public String getMysqlSslTrustKeystorePassword() {
        return this.mysqlSslTrustKeystorePassword;
    }

    public String getMysqlTablePrefix() {
        return this.mysqlTablePrefix;
    }

    public boolean isBlockNametag() {
        return this.blockNametag;
    }

    public boolean isUpdateChecker() {
        return this.updateChecker;
    }

    public boolean isGlobalSound() {
        return this.globalSound;
    }

}

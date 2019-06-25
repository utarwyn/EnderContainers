package fr.utarwyn.endercontainers.configuration;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.List;

/**
 * Configuration class. Reflects the config.yml
 *
 * @author Utarwyn
 * @since 2.0.0
 */
public class Configuration extends YamlFile {

    @Configurable
    private boolean enabled;

    @Configurable
    private boolean debug;

    @Configurable
    private String locale;

    @Configurable
    private List<String> disabledWorlds;

    @Configurable(key = "enderchests.max")
    private Integer maxEnderchests;

    @Configurable(key = "enderchests.default")
    private Integer defaultEnderchests;

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

    @Configurable(key = "mysql.tablePrefix")
    private String mysqlTablePrefix;

    @Configurable(key = "others.blockNametag")
    private boolean blockNametag;

    @Configurable(key = "others.updateChecker")
    private boolean updateChecker;

    @Configurable(key = "others.useExperimentalSavingSystem")
    private boolean useExperimentalSavingSystem;

    @Configurable(key = "others.globalSound")
    private boolean globalSound;

    Configuration(JavaPlugin plugin) {
        super(plugin);
    }

    @Override
    protected FileConfiguration getFileConfiguration() {
        if (!new File(this.plugin.getDataFolder(), "config.yml").exists()) {
            this.plugin.saveDefaultConfig();
        }

        return this.plugin.getConfig();
    }

    public boolean isEnabled() {
        return this.enabled;
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

    public String getMysqlTablePrefix() {
        return this.mysqlTablePrefix;
    }

    public boolean isBlockNametag() {
        return this.blockNametag;
    }

    public boolean isUpdateChecker() {
        return this.updateChecker;
    }

    public boolean isUseExperimentalSavingSystem() {
        return this.useExperimentalSavingSystem;
    }

    public boolean isGlobalSound() {
        return this.globalSound;
    }

}

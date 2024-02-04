package fr.utarwyn.endercontainers.configuration;

import fr.utarwyn.endercontainers.configuration.ui.EnderChestItem;
import fr.utarwyn.endercontainers.configuration.ui.EnderChestItemVariant;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Handles plugin configuration.
 *
 * @author Utarwyn
 * @since 2.0.0
 */
public class Configuration {

    private final String locale;
    private final List<String> disabledWorlds;

    private final int maxEnderchests;
    private final int defaultEnderchests;
    private final boolean useVanillaEnderchest;
    private final List<Material> forbiddenMaterials;

    private final EnderChestItem enderchestItem;
    private final List<EnderChestItemVariant> enderchestItemVariants;
    private final boolean numberingEnderchests;
    private final boolean onlyShowAccessibleEnderchests;

    private final boolean mysql;
    private final String mysqlHost;
    private final int mysqlPort;
    private final String mysqlUser;
    private final String mysqlPassword;
    private final String mysqlDatabase;
    private final boolean mysqlSsl;
    private final String mysqlSslKeystoreFile;
    private final String mysqlSslKeystorePassword;
    private final String mysqlSslTrustKeystoreFile;
    private final String mysqlSslTrustKeystorePassword;
    private final String mysqlTablePrefix;

    private final boolean blockNametag;
    private final boolean updateChecker;
    private final boolean globalSound;

    /**
     * Create a configuration object from plugin configuration.
     *
     * @param config plugin configuration object
     * @throws ConfigLoadingException thrown if configuration cannot be loaded
     */
    Configuration(FileConfiguration config) throws ConfigLoadingException {
        boolean legacyOnlyShowAccessible;

        this.locale = loadValue("locale", config::isString, config::getString);
        this.disabledWorlds = loadValue("disabledWorlds", config::isList, config::getStringList);

        this.maxEnderchests = loadValue("enderchests.max", config::isInt, config::getInt);
        this.defaultEnderchests = loadValue("enderchests.default", config::isInt, config::getInt);
        legacyOnlyShowAccessible = loadValue("enderchests.onlyShowAccessible", v -> true, config::getBoolean);
        this.useVanillaEnderchest = loadValue("enderchests.useVanillaEnderchest", config::isBoolean, config::getBoolean);
        this.forbiddenMaterials = loadValue(
                "enderchests.forbiddenMaterials",
                key -> config.isList(key) && config.getStringList(key).stream().allMatch(material -> Material.matchMaterial(material) != null),
                key -> config.getStringList(key).stream().map(Material::matchMaterial).collect(Collectors.toList())
        );

        this.enderchestItem = loadValue("ui.enderchestItem", config::isConfigurationSection,
                key -> new EnderChestItem(config.getConfigurationSection(key))
        );
        this.enderchestItemVariants = loadValue("ui.enderchestItem.variants", config::isList,
                key -> Objects.requireNonNull(config.getList(key)).stream()
                        .map(map -> new EnderChestItemVariant(this.enderchestItem, (Map<String, Object>) map))
                        .collect(Collectors.toList())
        );
        this.numberingEnderchests = loadValue("ui.enderchestItem.numbering", config::isBoolean, config::getBoolean);
        this.onlyShowAccessibleEnderchests = loadValue("ui.onlyShowAccessible", config::isBoolean, config::getBoolean)
                || legacyOnlyShowAccessible;

        this.mysql = loadValue("mysql.enabled", config::isBoolean, config::getBoolean);
        this.mysqlHost = loadValue("mysql.host", config::isString, config::getString);
        this.mysqlPort = loadValue("mysql.port", config::isInt, config::getInt);
        this.mysqlUser = loadValue("mysql.user", config::isString, config::getString);
        this.mysqlPassword = loadValue("mysql.password", config::isString, config::getString);
        this.mysqlDatabase = loadValue("mysql.database", config::isString, config::getString);
        this.mysqlSsl = loadValue("mysql.ssl.enabled", config::isBoolean, config::getBoolean);
        this.mysqlTablePrefix = loadValue("mysql.tablePrefix", config::isString, config::getString);

        if (this.mysqlSsl) {
            this.mysqlSslKeystoreFile = loadValue("mysql.ssl.keystore_file", config::isString, config::getString);
            this.mysqlSslKeystorePassword = loadValue("mysql.ssl.keystore_password", config::isString, config::getString);
            this.mysqlSslTrustKeystoreFile = loadValue("mysql.ssl.ca_keystore_file", config::isString, config::getString);
            this.mysqlSslTrustKeystorePassword = loadValue("mysql.ssl.ca_keystore_password", config::isString, config::getString);
        } else {
            this.mysqlSslKeystoreFile = null;
            this.mysqlSslKeystorePassword = null;
            this.mysqlSslTrustKeystoreFile = null;
            this.mysqlSslTrustKeystorePassword = null;
        }

        this.blockNametag = loadValue("others.blockNametag", config::isBoolean, config::getBoolean);
        this.updateChecker = loadValue("others.updateChecker", config::isBoolean, config::getBoolean);
        this.globalSound = loadValue("others.globalSound", config::isBoolean, config::getBoolean);
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

    public boolean isNumberingEnderchests() {
        return this.numberingEnderchests;
    }

    public List<Material> getForbiddenMaterials() {
        return this.forbiddenMaterials;
    }

    public EnderChestItem getEnderchestItem() {
        return this.enderchestItem;
    }

    public List<EnderChestItemVariant> getEnderchestItemVariants() {
        return this.enderchestItemVariants;
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

    private <T> T loadValue(String key, Predicate<String> checker, Function<String, T> getter) throws ConfigLoadingException {
        Throwable cause = null;
        if (checker.test(key)) {
            try {
                return getter.apply(key);
            } catch (Exception e) {
                cause = e;
            }
        }

        throw new ConfigLoadingException(String.format(
                "Cannot set value of config key %s for %s",
                key, getClass().getSimpleName().toLowerCase()
        ), cause);
    }

}

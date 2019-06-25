package fr.utarwyn.endercontainers.configuration;

import fr.utarwyn.endercontainers.compatibility.ServerVersion;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;

/**
 * Locale class. Reflects a locale .yml file.
 *
 * @author Utarwyn
 * @since 2.0.0
 */
public class Locale extends YamlFile {

    @Configurable(key = "commands.backups.created")
    private String backupCreated;

    @Configurable(key = "commands.backups.creation_starting")
    private String backupCreationStarting;

    @Configurable(key = "commands.backups.exists")
    private String backupExists;

    @Configurable(key = "commands.backups.info")
    private String backupInfo;

    @Configurable(key = "commands.backups.loaded")
    private String backupLoaded;

    @Configurable(key = "commands.backups.loading_starting")
    private String backupLoadingStarted;

    @Configurable(key = "commands.backups.removed")
    private String backupRemoved;

    @Configurable(key = "commands.backups.unknown")
    private String backupUnknown;

    @Configurable(key = "commands.backups.zero")
    private String backupZero;

    @Configurable(key = "commands.backups.label_name")
    private String backupLabelName;

    @Configurable(key = "commands.backups.label_date")
    private String backupLabelDate;

    @Configurable(key = "commands.backups.label_by")
    private String backupLabelBy;

    @Configurable(key = "commands.backups.label_loadcmd")
    private String backupLabelLoadCmd;

    @Configurable(key = "commands.backups.label_rmcmd")
    private String backupLabelRmCmd;

    @Configurable(key = "commands.config_reloaded")
    private String configReloaded;

    @Configurable(key = "commands.no_update")
    private String noUpdate;

    @Configurable(key = "menus.main_title")
    private String menuMainTitle;

    @Configurable(key = "menus.chest_title")
    private String menuChestTitle;

    @Configurable(key = "menus.pane_title")
    private String menuPaneTitle;

    @Configurable(key = "menus.chest_empty")
    private String menuChestEmpty;

    @Configurable(key = "menus.chest_full")
    private String menuChestFull;

    @Configurable(key = "menus.chest_locked")
    private String menuChestLocked;

    @Configurable(key = "menus.previous_page")
    private String menuPreviousPage;

    @Configurable(key = "menus.next_page")
    private String menuNextPage;

    @Configurable(key = "errors.noperm_open_chest")
    private String nopermOpenChest;

    @Configurable(key = "errors.noperm_console")
    private String nopermConsole;

    @Configurable(key = "errors.noperm_player")
    private String nopermPlayer;

    @Configurable(key = "errors.plugin_disabled")
    private String pluginDisabled;

    @Configurable(key = "errors.plugin_world_disabled")
    private String pluginWorldDisabled;

    @Configurable(key = "errors.cmd_invalid_parameter")
    private String cmdInvalidParameter;

    @Configurable(key = "errors.cmd_wrong_argument_count")
    private String cmdWrongArgumentCount;

    @Configurable(key = "dependencies.access_denied_factions")
    private String accessDeniedFactions;

    @Configurable(key = "dependencies.access_denied_plotsq")
    private String accessDeniedPlotSq;

    @Configurable(key = "miscellaneous.chest_nametag")
    private String chestNametag;

    Locale(JavaPlugin plugin) {
        super(plugin);
    }

    @Override
    public FileConfiguration getFileConfiguration() {
        // Create custom locale .yml file if not exists
        File localeFolder = new File(plugin.getDataFolder(), "locales/");
        File file = new File(localeFolder, Files.getConfiguration().getLocale() + ".yml");

        if (!file.exists()) {
            // File doesn't exists... use the template to pre-fill the data.
            try {
                if (!localeFolder.exists() && !localeFolder.mkdir()) return null;
                if (!file.createNewFile()) return null;

                InputStream in = plugin.getResource("locale.yml");
                if (in == null) return null;

                try (InputStreamReader isr = new InputStreamReader(in, StandardCharsets.UTF_8);
                     FileOutputStream out = new FileOutputStream(file);
                     OutputStreamWriter osw = new OutputStreamWriter(out, StandardCharsets.UTF_8)) {
                    char[] tempbytes = new char[512];
                    int readbytes = isr.read(tempbytes, 0, 512);

                    while (readbytes > -1) {
                        osw.write(tempbytes, 0, readbytes);
                        readbytes = isr.read(tempbytes, 0, 512);
                    }
                }
            } catch (IOException e) {
                this.plugin.getLogger().log(Level.SEVERE, "Cannot write the initial locale file on the disk", e);
            }
        }

        // And load all locale messages ...
        return YamlConfiguration.loadConfiguration(file);
    }

    @Override
    protected Object parseValue(String key, Object value) {
        if (value instanceof String) {
            String message = String.valueOf(value);

            if (ServerVersion.isOlderThan(ServerVersion.V1_9)) {
                message = new String(message.getBytes(), StandardCharsets.UTF_8);
            }

            return ChatColor.translateAlternateColorCodes('&', message);
        }

        return value;
    }

    public String getBackupCreated() {
        return this.backupCreated;
    }

    public String getBackupCreationStarting() {
        return this.backupCreationStarting;
    }

    public String getBackupExists() {
        return this.backupExists;
    }

    public String getBackupInfo() {
        return this.backupInfo;
    }

    public String getBackupLoaded() {
        return this.backupLoaded;
    }

    public String getBackupLoadingStarted() {
        return this.backupLoadingStarted;
    }

    public String getBackupRemoved() {
        return this.backupRemoved;
    }

    public String getBackupUnknown() {
        return this.backupUnknown;
    }

    public String getBackupZero() {
        return this.backupZero;
    }

    public String getBackupLabelName() {
        return this.backupLabelName;
    }

    public String getBackupLabelDate() {
        return this.backupLabelDate;
    }

    public String getBackupLabelBy() {
        return this.backupLabelBy;
    }

    public String getBackupLabelLoadCmd() {
        return this.backupLabelLoadCmd;
    }

    public String getBackupLabelRmCmd() {
        return this.backupLabelRmCmd;
    }

    public String getConfigReloaded() {
        return this.configReloaded;
    }

    public String getNoUpdate() {
        return this.noUpdate;
    }

    public String getMenuMainTitle() {
        return this.menuMainTitle;
    }

    public String getMenuChestTitle() {
        return this.menuChestTitle;
    }

    public String getMenuPaneTitle() {
        return this.menuPaneTitle;
    }

    public String getMenuChestEmpty() {
        return this.menuChestEmpty;
    }

    public String getMenuChestFull() {
        return this.menuChestFull;
    }

    public String getMenuChestLocked() {
        return this.menuChestLocked;
    }

    public String getMenuPreviousPage() {
        return this.menuPreviousPage;
    }

    public String getMenuNextPage() {
        return this.menuNextPage;
    }

    public String getNopermOpenChest() {
        return this.nopermOpenChest;
    }

    public String getNopermConsole() {
        return this.nopermConsole;
    }

    public String getNopermPlayer() {
        return this.nopermPlayer;
    }

    public String getPluginDisabled() {
        return this.pluginDisabled;
    }

    public String getPluginWorldDisabled() {
        return this.pluginWorldDisabled;
    }

    public String getCmdInvalidParameter() {
        return this.cmdInvalidParameter;
    }

    public String getCmdWrongArgumentCount() {
        return this.cmdWrongArgumentCount;
    }

    public String getAccessDeniedFactions() {
        return this.accessDeniedFactions;
    }

    public String getAccessDeniedPlotSq() {
        return this.accessDeniedPlotSq;
    }

    public String getChestNametag() {
        return this.chestNametag;
    }

}

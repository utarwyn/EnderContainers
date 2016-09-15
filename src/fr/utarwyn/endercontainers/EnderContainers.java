package fr.utarwyn.endercontainers;

import fr.utarwyn.endercontainers.commands.EnderChestCommand;
import fr.utarwyn.endercontainers.commands.EnderContainersCommand;
import fr.utarwyn.endercontainers.database.Database;
import fr.utarwyn.endercontainers.dependencies.CitizensIntegration;
import fr.utarwyn.endercontainers.listeners.EnderChestListener;
import fr.utarwyn.endercontainers.listeners.NameTagTask;
import fr.utarwyn.endercontainers.listeners.PluginListener;
import fr.utarwyn.endercontainers.managers.DependenciesManager;
import fr.utarwyn.endercontainers.managers.EnderchestsManager;
import fr.utarwyn.endercontainers.managers.LocalesManager;
import fr.utarwyn.endercontainers.managers.MysqlManager;
import fr.utarwyn.endercontainers.utils.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;

public class EnderContainers extends JavaPlugin {

    private static EnderContainers instance;
    private static ConfigClass configClass;
    private static Database database;

    // Managers
    private EnderchestsManager enderchestsManager;
    private DependenciesManager dependenciesManager;
    private MysqlManager mysqlManager;
    private LocalesManager localesManager;

    // Dependencies
    private CitizensIntegration citizensIntegration = new CitizensIntegration();

    public NameTagTask nameTagTask = new NameTagTask();

    public String newVersion = null;
    public HashMap<String, String> newLocaleKeys = new HashMap<>();

    public void onEnable() {
        EnderContainers.instance = this;

        this.localesManager = new LocalesManager();

        loadMainConfig();
        loadBackupsConfig();
        loadCommands();
        loadManagers();
        loadListeners();
        loadTasks();

        loadDependencies();

        loadMysql();
        loadMetrics();

        checkForUpdates();
    }

    public void onDisable() {
        try {

            // Clear nametags
            HashMap<UUID, FloatingTextUtils.FloatingText> nametags = EnderContainers.getInstance().nameTagTask.getActiveNametags();
            for(UUID uuid : nametags.keySet()){
                FloatingTextUtils.FloatingText ft = nametags.get(uuid);
                ft.remove();
            }

            // Save opened enderchests
            CoreUtils.log(Config.pluginPrefix + "§7Save all opened enderchests...");
            EnderChestUtils.saveOpenedEnderchests();
            CoreUtils.log(Config.pluginPrefix + "§aAll enderchests are now saved in the config ! See you soon :P");

            // Close MySQL source pool
            if(Database.source != null && Database.isConnected()) Database.source.close();

        } catch(Exception ex) {
            CoreUtils.log(Config.pluginPrefix + "§cAn error occured during the disabling of the plugin. Please report this error to the plugin's owner.", true);
        }
    }


    private void loadMainConfig() {
        EnderContainers.configClass = new ConfigClass(EnderContainers.getInstance());

        // From config.yml to locale/*.yml (from 1.0.6c version)
        if (getConfigClass().contains("main", "enderchests.mainTitle")) {
            newLocaleKeys.put("enderchest_main_gui_title", getConfigClass().getString("main", "enderchests.mainTitle"));
            getConfigClass().removePath("main", "enderchests.mainTitle");
        }else{
            newLocaleKeys.put("enderchest_main_gui_title", localesManager.getDefaultMessages().get("enderchest_main_gui_title"));
        }

        // Removing from 1.0.6c version
        if (getConfigClass().contains("main", "enderchests.allowDoubleChest"))
            getConfigClass().removePath("main", "enderchests.allowDoubleChest");

        newLocaleKeys.put("enderchest_glasspane_title", localesManager.getDefaultMessages().get("enderchest_glasspane_title"));
        newLocaleKeys.put("error_plugin_disabled_world", localesManager.getDefaultMessages().get("error_plugin_disabled_world"));
        newLocaleKeys.put("enderchest_default_glasspane_title", localesManager.getDefaultMessages().get("enderchest_default_glasspane_title"));


        getConfigClass().set("main", "saveVersion", getDescription().getVersion());

        getConfigClass().saveConfig("main");


        Config.enabled = getConfigClass().getBoolean("main", "enabled");
        Config.debug = getConfigClass().getBoolean("main", "debug");
        Config.prefix = ChatColor.translateAlternateColorCodes('&', getConfigClass().getString("main", "prefix"));
        Config.disabledWorlds = getConfigClass().getStringList("main", "disabledWorlds");
        Config.pluginLocale = getConfigClass().getString("main", "locale");

        if (getConfigClass().getInt("main", "enderchests.max") <= 54 && getConfigClass().getInt("main", "enderchests.max") > 0)
            Config.maxEnderchests = getConfigClass().getInt("main", "enderchests.max");
        else Config.maxEnderchests = 54;
        if (getConfigClass().getInt("main", "enderchests.default") <= 54 && getConfigClass().getInt("main", "enderchests.default") >= 0)
            Config.defaultEnderchestsNumber = getConfigClass().getInt("main", "enderchests.default");
        else Config.defaultEnderchestsNumber = 1;

        Config.mysql     = getConfigClass().getBoolean("main", "mysql.enabled");
        Config.DB_HOST   = getConfigClass().getString("main", "mysql.host");
        Config.DB_PORT   = getConfigClass().getInt("main", "mysql.port");
        Config.DB_USER   = getConfigClass().getString("main", "mysql.user");
        Config.DB_PASS   = getConfigClass().getString("main", "mysql.password");
        Config.DB_BDD    = getConfigClass().getString("main", "mysql.database");
        Config.DB_PREFIX = getConfigClass().getString("main", "mysql.tablePrefix");

        Config.blockNametag      = getConfigClass().getBoolean("main", "others.blocknametag");
        Config.openingChestSound = getConfigClass().getString("main", "others.openingChestSound");
        Config.closingChestSound = getConfigClass().getString("main", "others.closingChestSound");
        Config.updateChecker     = getConfigClass().getBoolean("main", "others.updateChecker");

        // CoreUtils.beautifyConfig();
    }

    private void loadBackupsConfig() {
        if(!Config.mysql){
            getConfigClass().loadConfigFile("backups.yml");
            getConfigClass().loadConfigFile("players.yml");
        }
    }

    private void loadCommands() {
        getCommand("endercontainers").setExecutor(new EnderContainersCommand());
        getCommand("enderchest").setExecutor(new EnderChestCommand());
    }

    private void loadManagers() {
        this.dependenciesManager = new DependenciesManager();
        this.enderchestsManager  = new EnderchestsManager();
        this.mysqlManager        = new MysqlManager();

        this.localesManager.loadMessages();
    }

    private void loadListeners() {
        getServer().getPluginManager().registerEvents(new EnderChestListener(), this);
        getServer().getPluginManager().registerEvents(new PluginListener(), this);
    }

    private void loadDependencies(){
        if(getDependenciesManager().dependencyIsLoaded("Citizens")) {
            citizensIntegration.load();
            getServer().getPluginManager().registerEvents(citizensIntegration, this);
        }
    }

    private void loadTasks(){
        if(!Config.blockNametag) return;
        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, nameTagTask, 0, 5L);
    }

    private void loadMysql(){
        this.getServer().getScheduler().runTaskAsynchronously(this, new Runnable() {
            @Override
            public void run() {
                if (Config.mysql) {
                    Config.enabled = false;

                    // Connect to database
                    database = new Database();
                    mysqlManager.setDatabase(database);

                    if(Config.mysql){
                        String sqlVersion = database.getMySQLVersion();
                        int pointIndex    = (sqlVersion.lastIndexOf('.') == -1) ? (sqlVersion.length() - 1) : sqlVersion.lastIndexOf('.');
                        Double version    = Double.valueOf(sqlVersion.substring(0, pointIndex));
                        String collation  = (version >= 5.5) ? "utf8mb4_unicode_ci" : "utf8_unicode_ci";

                        if (!database.tableExists(Config.DB_PREFIX + "enderchests")) {
                            database.request("CREATE TABLE `" + Config.DB_PREFIX + "enderchests` (`id` INT(11) NOT NULL AUTO_INCREMENT, `items` MEDIUMTEXT NULL, `slots_used` INT(2) NOT NULL DEFAULT 0, `last_opening_time` TIMESTAMP NULL, `last_save_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, `player_uuid` VARCHAR(36) NULL, `enderchest_id` TINYINT(2) NOT NULL DEFAULT '0', PRIMARY KEY (`id`)) COLLATE='" + collation + "' ENGINE=InnoDB;");
                            CoreUtils.log(Config.pluginPrefix + "§aMysql: table `" + Config.DB_PREFIX + "enderchests` created.");
                        }else{
                            CoreUtils.log(Config.pluginPrefix + "§eMysql: table §6`" + Config.DB_PREFIX + "enderchests`§e already exists.");
                        }

                        if (!database.tableExists(Config.DB_PREFIX + "players")) {
                            database.request("CREATE TABLE `" + Config.DB_PREFIX + "players` (`id` INT(11) NOT NULL AUTO_INCREMENT, `player_name` VARCHAR(60) NULL, `player_uuid` VARCHAR(36) NULL, `accesses` TEXT NULL, PRIMARY KEY (`id`)) COLLATE='" + collation + "' ENGINE=InnoDB;");
                            CoreUtils.log(Config.pluginPrefix + "§aMysql: table `" + Config.DB_PREFIX + "players` created.");
                        }else{
                            CoreUtils.log(Config.pluginPrefix + "§eMysql: table §6`" + Config.DB_PREFIX + "players`§e already exists.");
                        }

                        if (!database.tableExists(Config.DB_PREFIX + "backups")) {
                            database.request("CREATE TABLE `" + Config.DB_PREFIX + "backups` (`id` INT(11) NOT NULL AUTO_INCREMENT, `name` VARCHAR(255) NULL, `date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, `type` VARCHAR(255) NULL, `data` MEDIUMTEXT NULL, `created_by` VARCHAR(60) NULL, PRIMARY KEY (`id`)) COLLATE='" + collation + "' ENGINE=InnoDB;");
                            CoreUtils.log(Config.pluginPrefix + "§aMysql: table `" + Config.DB_PREFIX + "backups` created.");
                        }else{
                            CoreUtils.log(Config.pluginPrefix + "§eMysql: table §6`" + Config.DB_PREFIX + "backups`§e already exists.");
                        }

                        for (Player player : Bukkit.getOnlinePlayers())
                            EnderContainers.getMysqlManager().updatePlayerUUID(player);
                    }

                    Config.enabled = true;
                }else{
                    for(Player player : Bukkit.getOnlinePlayers())
                        EnderContainers.getEnderchestsManager().savePlayerInfo(player);
                }
            }
        });
    }

    private void loadMetrics(){
        try {
            Metrics metrics = new Metrics(this);
            metrics.start();
        } catch (IOException e) {
            CoreUtils.errorMessage(getServer().getConsoleSender(), "Plugin Metrics error : failed to submit stats.");
        }
    }


    private void checkForUpdates(){
        if(!Config.updateChecker) return;

        String newVersion = new Updater(this).getNewVersion();

        if(newVersion != null){
            this.newVersion = newVersion;
            CoreUtils.log(Config.pluginPrefix + "§aThere is a newer version available: §2§l" + newVersion + "§a.", true);
        }
    }



    public static EnderContainers getInstance() {
        return EnderContainers.instance;
    }

    public static EnderchestsManager getEnderchestsManager(){
        return EnderContainers.instance.enderchestsManager;
    }
    public static MysqlManager getMysqlManager(){
        return EnderContainers.instance.mysqlManager;
    }
    public static DependenciesManager getDependenciesManager() {
        return EnderContainers.instance.dependenciesManager;
    }

    public static String __(String key){
        return EnderContainers.getInstance().localesManager.get(key);
    }

    public static ConfigClass getConfigClass() {
        return configClass;
    }
    public static CitizensIntegration getCitizensIntegration(){
        return EnderContainers.getInstance().citizensIntegration;
    }

    public void reloadConfiguration() {
        EnderContainers.getConfigClass().reloadConfigs();

        loadMainConfig();
        loadBackupsConfig();

        loadMysql();
    }

    public static Boolean hasMysql(){
        return (Config.mysql && (database != null && Database.isConnected()));
    }
    public static Database getDB() {
        return database;
    }
}

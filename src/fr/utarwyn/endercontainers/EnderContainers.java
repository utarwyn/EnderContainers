package fr.utarwyn.endercontainers;

import fr.utarwyn.endercontainers.commands.EnderChestCommand;
import fr.utarwyn.endercontainers.commands.EnderContainersCommand;
import fr.utarwyn.endercontainers.database.Database;
import fr.utarwyn.endercontainers.listeners.EnderChestListener;
import fr.utarwyn.endercontainers.listeners.NameTagTask;
import fr.utarwyn.endercontainers.listeners.PluginListener;
import fr.utarwyn.endercontainers.managers.DependenciesManager;
import fr.utarwyn.endercontainers.managers.EnderchestsManager;
import fr.utarwyn.endercontainers.utils.Config;
import fr.utarwyn.endercontainers.utils.ConfigClass;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

public class EnderContainers extends JavaPlugin {

    private static EnderContainers instance;
    private static ConfigClass configClass;
    private static Database database;

    // Managers
    public EnderchestsManager enderchestsManager;
    public DependenciesManager dependenciesManager;

    public void onEnable() {
        EnderContainers.instance = this;

        loadMainConfig();
        loadBackupsConfig();
        loadCommands();
        loadManagers();
        loadListeners();
        loadTasks();

        // Enable Mysql Module
        if (Config.mysql) {
            database = new Database();

            checkDatabase();
        }
    }

    public void onDisable() {

    }


    public void loadMainConfig() {
        EnderContainers.configClass = new ConfigClass(EnderContainers.getInstance());

        if (!getConfigClass().contains("main", "enabled"))
            getConfigClass().set("main", "enabled", Config.enabled);
        if (!getConfigClass().contains("main", "debug"))
            getConfigClass().set("main", "debug", Config.debug);
        if (!getConfigClass().contains("main", "prefix"))
            getConfigClass().set("main", "prefix", "&8[&6EnderContainers&8] &7");
        if (!getConfigClass().contains("main", "enderchests.max"))
            getConfigClass().set("main", "enderchests.max", Config.maxEnderchests);
        if (!getConfigClass().contains("main", "enderchests.mainTitle"))
            getConfigClass().set("main", "enderchests.mainTitle", Config.mainEnderchestTitle);
        if (!getConfigClass().contains("main", "enderchests.enderchestTitle"))
            getConfigClass().set("main", "enderchests.enderchestTitle", Config.enderchestTitle);

        if (!getConfigClass().contains("main", "mysql.enabled"))
            getConfigClass().set("main", "mysql.enabled", Config.mysql);
        if (!getConfigClass().contains("main", "mysql.host"))
            getConfigClass().set("main", "mysql.host", Config.DB_HOST);
        if (!getConfigClass().contains("main", "mysql.port"))
            getConfigClass().set("main", "mysql.port", Config.DB_PORT);
        if (!getConfigClass().contains("main", "mysql.user"))
            getConfigClass().set("main", "mysql.user", Config.DB_USER);
        if (!getConfigClass().contains("main", "mysql.password"))
            getConfigClass().set("main", "mysql.password", Config.DB_PASS);
        if (!getConfigClass().contains("main", "mysql.database"))
            getConfigClass().set("main", "mysql.database", Config.DB_BDD);

        if (!getConfigClass().contains("main", "others.blocknametag"))
            getConfigClass().set("main", "others.blocknametag", Config.blockNametag);

        Config.enabled = getConfigClass().getBoolean("main", "enabled");
        Config.debug = getConfigClass().getBoolean("main", "debug");
        Config.prefix = ChatColor.translateAlternateColorCodes('&', getConfigClass().getString("main", "prefix"));
        if (getConfigClass().getInt("main", "enderchests.max") <= 54)
            Config.maxEnderchests = getConfigClass().getInt("main", "enderchests.max");
        else Config.maxEnderchests = 54;
        Config.mainEnderchestTitle = ChatColor.translateAlternateColorCodes('&', getConfigClass().getString("main", "enderchests.mainTitle"));
        Config.enderchestTitle = ChatColor.translateAlternateColorCodes('&', getConfigClass().getString("main", "enderchests.enderchestTitle"));

        Config.mysql = getConfigClass().getBoolean("main", "mysql.enabled");
        Config.DB_HOST = getConfigClass().getString("main", "mysql.host");
        Config.DB_PORT = getConfigClass().getInt("main", "mysql.port");
        Config.DB_USER = getConfigClass().getString("main", "mysql.user");
        Config.DB_PASS = getConfigClass().getString("main", "mysql.password");
        Config.DB_BDD = getConfigClass().getString("main", "mysql.database");

        Config.blockNametag = getConfigClass().getBoolean("main", "others.blocknametag");
    }

    public void loadBackupsConfig() {
        getConfigClass().loadConfigFile("backups.yml");
    }

    public void loadCommands() {
        getCommand("endercontainers").setExecutor(new EnderContainersCommand());
        getCommand("enderchest").setExecutor(new EnderChestCommand());
    }

    public void loadManagers() {
        this.dependenciesManager = new DependenciesManager();
        this.enderchestsManager = new EnderchestsManager();
    }

    public void loadListeners() {
        getServer().getPluginManager().registerEvents(new EnderChestListener(), this);
        getServer().getPluginManager().registerEvents(new PluginListener(), this);
    }

    public void loadTasks(){
        if(!Config.blockNametag) return;

        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new NameTagTask(), 10L, 0);
    }



    public static EnderContainers getInstance() {
        return EnderContainers.instance;
    }

    public static EnderchestsManager getEnderchestsManager(){
        return EnderContainers.instance.enderchestsManager;
    }

    public static ConfigClass getConfigClass() {
        return EnderContainers.configClass;
    }

    public static Database getDB() {
        return database;
    }

    public DependenciesManager getDependenciesManager() {
        return this.dependenciesManager;
    }


    public void reloadConfiguration() {
        EnderContainers.getConfigClass().reloadConfigs();

        loadMainConfig();
        loadBackupsConfig();
    }

    public void checkDatabase() {
        EnderContainers.getDB().connect();
    }

}

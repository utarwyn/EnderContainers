package fr.utarwyn.endercontainers.managers;

import fr.utarwyn.endercontainers.EnderContainers;
import fr.utarwyn.endercontainers.utils.Config;
import org.bukkit.ChatColor;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class LocalesManager {

    private Map<String, String> messages = new HashMap<>();

    public LocalesManager(){
        this.loadMessages();
    }


    public void loadMessages(){
        String locale = Config.pluginLocale;
        String file   = "locales/" + locale + ".yml";

        EnderContainers.getConfigClass().loadConfigFile(file);
        Set<String> keys = EnderContainers.getConfigClass().getKeys(file, false);

        if(keys.size() == 0 && locale.equals("en")){
            keys = this.generateDefaultMessages();
        }else if(keys.size() == 0){
            Config.pluginLocale = "en";
            this.loadMessages();
            return;
        }

        for(String key : keys){
            messages.put(key, EnderContainers.getConfigClass().getString(file, key));
        }
    }
    public Set<String> generateDefaultMessages(){
        String locale = Config.pluginLocale;
        String file   = "locales/" + locale + ".yml";
        Map<String, String> messages = new TreeMap<>();

        messages.put("cmd_backup_creation_starting", "Starting backup creation...");
        messages.put("cmd_backup_created", "&aBackup created with the name &b%backup_name%&a.");
        messages.put("cmd_backup_exists_error", "Backup &6%backup_name% &calready exists. Please change name.");
        messages.put("cmd_backup_loading_starting", "Starting backup loading...");
        messages.put("cmd_backup_folder_unknown", "Backup folder of &6%backup_name%&c not exist.");
        messages.put("cmd_backup_unknown", "Backup &6%backup_name%&c is undefined.");
        messages.put("cmd_backup_loaded", "&aBackup &b%backup_name%&a loaded indefinitly.");
        messages.put("cmd_backup_removed", "&aBackup &b%backup_name%&a removed.");
        messages.put("cmd_backup_info", "&7To show information about a backup: &c%command%");
        messages.put("cmd_backup_label_name", "Name");
        messages.put("cmd_backup_label_creationdate", "Creation date");
        messages.put("cmd_backup_label_backuptype", "Backup Type");
        messages.put("cmd_backup_label_backuppath", "Backup path");
        messages.put("cmd_backup_label_loadcmd", "&8 >> To load this backup");
        messages.put("cmd_backup_label_removecmd", "&8 >> To remove this backup");
        messages.put("cmd_backup_label_createdby", "Created by");
        messages.put("cmd_nobackup", "No backup. Create backup : &6%command%");
        messages.put("cmd_update_found", "&7Update found : v&e%version%&7. To do this update : &6%command%");
        messages.put("cmd_update_notfound", "There is no update at this time. Retry later.");
        messages.put("cmd_update_install_error", "Before install any update, do &6%command% &cto check for updates.");
        messages.put("cmd_config_reloaded", "Configuration reloaded !");
        messages.put("cmd_unknown_error", "Unknown command");

        messages.put("enderchest_locked", "This enderchest is locked.");
        messages.put("enderchest_empty", "&2This enderchest is empty.");
        messages.put("enderchest_inventoryfull", "&4Inventory full !");
        messages.put("enderchest_show_contents", "&aClick to show this enderchest.");
        messages.put("enderchest_player_denied", "&cThe player don't have access to this chest.");
        messages.put("enderchest_player_never_connected", "This player has never played on the server ! Please retry.");
        messages.put("enderchest_nametag", "&6&l%enderchests%&r&e enderchest%plurial% available");

        messages.put("error_command_usage", "Usage");
        messages.put("error_player_noperm", "You don't have the permission to do this.");
        messages.put("error_player_denied", "You don't have access to this command.");
        messages.put("error_console_denied", "You must be a player to do this.");
        messages.put("error_access_denied_factions", "You can't use this EnderChest here.");
        messages.put("error_cannot_open_enderchest", "You can't open this EnderChest !");
        messages.put("error_unknown_enderchest", "Enderchest %enderchest% doesn't exist.");
        messages.put("error_plugin_disabled", "The plugin is actually disabled. Please wait.");

        messages.put("help_enderchest_cmd", "Open your enderchest");
        messages.put("help_open_enderchest_cmd", "Open an enderchest");
        messages.put("help_backups_cmd", "List backups");
        messages.put("help_create_backup_cmd", "Create a backup");
        messages.put("help_backup_info_cmd", "Show backup information");
        messages.put("help_backup_load_cmd", "Load a backup (indefinitly)");
        messages.put("help_remove_backup_cmd", "Remove a backup");
        messages.put("help_view_config_cmd", "View config");
        messages.put("help_updates_check_cmd", "Check for update");
        messages.put("help_reload_plugin_cmd", "Reload plugin configuration");

        messages.put("other_new_update", "There is a newer version of the plugin");
        messages.put("other_new_update_line2", "&7Type &e%command% &7in the chat to start the update.");

        EnderContainers.getConfigClass().setAutoSaving = false;
        for(String key : messages.keySet()) {
            EnderContainers.getConfigClass().set(file, key, messages.get(key));
        }

        EnderContainers.getConfigClass().saveConfig(file);
        EnderContainers.getConfigClass().setAutoSaving = true;

        return messages.keySet();
    }


    public String get(String key){
        if(messages.containsKey(key)) return ChatColor.translateAlternateColorCodes('&', messages.get(key));
        else return "%undefined%";
    }

}

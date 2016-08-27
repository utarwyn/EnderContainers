package fr.utarwyn.endercontainers.commands;

import fr.utarwyn.endercontainers.EnderContainers;
import fr.utarwyn.endercontainers.database.DatabaseSet;
import fr.utarwyn.endercontainers.utils.*;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.List;

public class EnderContainersCommand implements CommandExecutor {

    private boolean updateChecked = false;

    @SuppressWarnings("deprecation")
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!Config.enabled) {
            CoreUtils.errorMessage(sender, EnderContainers.__("error_plugin_disabled"));
            return true;
        }

        if (args.length >= 1) {
            if (args[0].equalsIgnoreCase("help")) {
                if (args.length < 2) {
                    getHelp(sender, 1);
                } else {
                    if (StringUtils.isNumeric(args[1]))
                        getHelp(sender, Integer.parseInt(args[1]));
                    else
                        getHelp(sender, 0);
                }
            } else if (args[0].equalsIgnoreCase("open")) {
                if(!(sender instanceof Player)){CoreUtils.consoleDenied(sender);return true;}
                Player p = (Player) sender;

                if (args.length <= 1) {
                    CoreUtils.errorMessage(p, EnderContainers.__("error_command_usage") + ": /endc open <player>");
                    return true;
                }
                if (!CoreUtils.playerHasPerm(p, "openchests") && !p.isOp()) {
                    CoreUtils.accessDenied(p);
                    return true;
                }
                if (Config.disabledWorlds.contains(p.getWorld().getName())) {
                    PluginMsg.pluginDisabledInWorld(p);
                    return true;
                }

                String playername = args[1];
                Player playerToSpec = Bukkit.getPlayer(playername);

                // Check if player spectated is online or not
                if (playerToSpec == null || !playerToSpec.isOnline()) {
                    EnderContainers.getEnderchestsManager().openOfflinePlayerMainMenu(p, playername);
                    return true;
                }

                EnderContainers.getEnderchestsManager().openPlayerMainMenu(p, playerToSpec);
            } else if (args[0].equalsIgnoreCase("backups")) {
                if (!CoreUtils.senderHasPerm(sender, "backups.view")) {
                    CoreUtils.accessDenied(sender);
                    return true;
                }
                viewBackups(sender);
            } else if (args[0].equalsIgnoreCase("createbackup")) {
                if (!CoreUtils.senderHasPerm(sender, "backups.create")) {
                    CoreUtils.accessDenied(sender);
                    return true;
                }
                if (args.length < 2) {
                    CoreUtils.errorMessage(sender, EnderContainers.__("error_command_usage") + ": /endc createbackup <name>");
                    return true;
                } else {
                    String name = args[1];

                    sender.sendMessage(Config.prefix + EnderContainers.__("cmd_backup_creation_starting"));
                    if (EnderChestUtils.createBackup(name, sender))
                        sender.sendMessage(Config.prefix + EnderContainers.__("cmd_backup_created").replace("%backup_name%", name));
                    else
                        CoreUtils.errorMessage(sender, EnderContainers.__("cmd_backup_exists_error").replace("%backup_name%", name));
                }
            } else if (args[0].equalsIgnoreCase("loadbackup")) {
                if (!CoreUtils.senderHasPerm(sender, "backups.load")) {
                    CoreUtils.accessDenied(sender);
                    return true;
                }
                if (args.length < 2) {
                    CoreUtils.errorMessage(sender, EnderContainers.__("error_command_usage") + ": /endc loadbackup <name>");
                    return true;
                } else {
                    String name = args[1];

                    sender.sendMessage(Config.prefix + EnderContainers.__("cmd_backup_loading_starting"));

                    EnderChestUtils.loadBackup(name, sender);
                }
            } else if (args[0].equalsIgnoreCase("backup")) {
                if (!CoreUtils.senderHasPerm(sender, "backups.info")) {
                    CoreUtils.accessDenied(sender);
                    return true;
                }
                if (args.length < 2) {
                    CoreUtils.errorMessage(sender, EnderContainers.__("error_command_usage") + ": /endc backup <name>");
                    return true;
                } else {
                    String name = args[1];
                    getBackupInformation(sender, name);
                }
            } else if (args[0].equalsIgnoreCase("rmbackup")) {
                if (!CoreUtils.senderHasPerm(sender, "backups.remove")) {
                    CoreUtils.accessDenied(sender);
                    return true;
                }
                if (args.length < 2) {
                    CoreUtils.errorMessage(sender, EnderContainers.__("error_command_usage") + ": /endc rmbackup <name>");
                    return true;
                } else {
                    String name = args[1];
                    EnderChestUtils.removeBackup(name, sender);
                }
            } else if (args[0].equalsIgnoreCase("config")) {
                getStringConfig(sender);
            } else if (args[0].equalsIgnoreCase("update")) {
                if (!CoreUtils.senderHasPerm(sender, "update")) {
                    CoreUtils.accessDenied(sender);
                    return true;
                }
                if (args.length >= 2) {
                    if (args[1].equalsIgnoreCase("install")) {
                        if (!updateChecked) {
                            CoreUtils.errorMessage(sender, EnderContainers.__("cmd_update_install_error").replace("%command%", "/endc update"));
                            return true;
                        } else {
                            doUpdate(sender);
                        }
                    } else {
                        CoreUtils.errorMessage(sender, EnderContainers.__("error_command_usage") + ": /endc update [install]");
                        return true;
                    }
                } else {
                    checkForUpdate(sender);
                }
            } else if (args[0].equalsIgnoreCase("reload")) {
                if (!CoreUtils.senderHasPerm(sender, "reload")) {
                    CoreUtils.accessDenied(sender);
                    return true;
                }
                EnderContainers.getInstance().reloadConfiguration();
                sender.sendMessage(Config.prefix + "§a" + EnderContainers.__("cmd_config_reloaded"));
            } else if (args[0].equalsIgnoreCase("npc") && EnderContainers.getDependenciesManager().dependencyIsLoaded("Citizens")) {
                if(!(sender instanceof Player)){CoreUtils.consoleDenied(sender);return true;}

                if (!sender.isOp()) {
                    CoreUtils.accessDenied(sender);
                    return true;
                }

                if(args.length <= 1){
                    CoreUtils.errorMessage(sender, EnderContainers.__("error_command_usage") + ": /endc npc <link|info|unlink>");
                    return true;
                }

                EnderContainers.getCitizensIntegration().onCommand((Player) sender, args[1], Arrays.copyOfRange(args, 2, args.length));
            } else {
                sender.sendMessage(Config.prefix + "§c" + EnderContainers.__("cmd_unknown_error") + ": §6/endercontainers help§c.");
            }
        } else {
            sender.sendMessage(Config.prefix + "Created by §3Utarwyn§7. Version §e" + EnderContainers.getInstance().getDescription().getVersion() + "§7.");
            sender.sendMessage(Config.prefix + "To show plugin's help: §6/endercontainers help§7.");
        }

        return true;
    }


    private void getHelp(CommandSender p, int page) {
        int maxpage = 2;
        if (maxpage < page) page = maxpage;

        p.sendMessage(" ");
        p.sendMessage("§7 ==============§8 [§6EnderContainers§r | §9" + page + "/" + maxpage + "§8] §7============== ");
        p.sendMessage(" ");

        if (page == 1) {
            sendFormattedHelpLine(p, EnderContainers.__("help_enderchest_cmd"), "§e/enderchest [number]", "*");
            sendFormattedHelpLine(p, EnderContainers.__("help_open_enderchest_cmd"), "§b/endc open <player>", "backups.openchests");

            p.sendMessage(" ");

            sendFormattedHelpLine(p, EnderContainers.__("help_backups_cmd"), "§b/endc backups", "backups.view");
            sendFormattedHelpLine(p, EnderContainers.__("help_create_backup_cmd"), "§b/endc createbackup <name>", "backups.create");
            sendFormattedHelpLine(p, EnderContainers.__("help_backup_info_cmd"), "§b/endc backup <name>", "backups.info");
        } else if (page == 2) {
            sendFormattedHelpLine(p, EnderContainers.__("help_backup_load_cmd"), "§b/endc loadbackup <name>", "backups.load");
            sendFormattedHelpLine(p, EnderContainers.__("help_remove_backup_cmd"), "§b/endc rmbackup <name>", "backups.remove");

            p.sendMessage(" ");

            sendFormattedHelpLine(p, EnderContainers.__("help_view_config_cmd"), "§b/endc config", null, true);
            sendFormattedHelpLine(p, EnderContainers.__("help_updates_check_cmd"), "§e/endc update", "update");
            sendFormattedHelpLine(p, EnderContainers.__("help_reload_plugin_cmd"), "§e/endc reload", null, true);
        }

        p.sendMessage(" ");
    }
    private void sendFormattedHelpLine(CommandSender sender, String title, String command, String perm){
        sendFormattedHelpLine(sender, title, command, perm, false);
    }
    private void sendFormattedHelpLine(CommandSender sender, String title, String command, String perm, Boolean isOp){
        String message       = "§7 - " + title + " : ";
        String hiddenCommand = "§c§k";

        for(int i = 0; i < command.length(); i++) hiddenCommand += "-";

        if((perm != null && perm.equals("*")) || (perm != null && CoreUtils.senderHasPerm(sender, perm)) || (isOp && (sender instanceof Player) && sender.isOp()) || sender instanceof ConsoleCommandSender)
            message += command;
        else
            message += hiddenCommand;

        sender.sendMessage(message);
    }


    private void getStringConfig(CommandSender p) {
        if (!p.isOp()) {
            CoreUtils.accessDenied(p);
            return;
        }

        p.sendMessage("§7=========§8[§6EnderContainers / Configuration§8]§7=========");
        p.sendMessage(" ");

        String rEna = "§cfalse";
        if (Config.enabled) rEna = "§atrue";
        String rDeb = "§cfalse";
        if (Config.debug) rDeb = "§atrue";

        p.sendMessage("§7 - Enabled = " + rEna);
        p.sendMessage("§7 - Debug = " + rDeb);
        p.sendMessage("§7 - PluginPrefix = §r" + ChatColor.stripColor(Config.prefix));
        p.sendMessage("§7 - DisabledWorlds = §r" + Arrays.asList(Config.disabledWorlds.toArray()));

        p.sendMessage(" ");

        p.sendMessage("§7 - SaveDir = §r" + Config.saveDir);
        p.sendMessage("§7 - MaxEnderchests = §r" + Config.maxEnderchests);
        p.sendMessage("§7 - DefaultEnderchestsNumber = §r" + Config.defaultEnderchestsNumber);

        p.sendMessage(" ");

        p.sendMessage("§7 - EnderchestOpenPerm = §r" + Config.enderchestOpenPerm);
        p.sendMessage("§7 - MainEnderchestTitle = §r" + EnderContainers.__("enderchest_main_gui_title"));
        p.sendMessage("§7 - EnderchestTitle = §r" + EnderContainers.__("enderchest_gui_title"));
    }

    private void checkForUpdate(final CommandSender p) {
        final String version = EnderContainers.getInstance().getDescription().getVersion();

        EnderContainers.getInstance().getServer().getScheduler().runTaskAsynchronously(EnderContainers.getInstance(), new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL(Config.updateBase + "version");
                    InputStream is = url.openStream();
                    ByteArrayOutputStream os = new ByteArrayOutputStream();
                    byte[] buf = new byte[4096];
                    int n;
                    while ((n = is.read(buf)) >= 0)
                        os.write(buf, 0, n);
                    os.close();
                    is.close();
                    byte[] data = os.toByteArray();

                    String newVersion = new String(data);

                    if (!newVersion.equalsIgnoreCase(version)) { // Do an update
                        p.sendMessage(Config.prefix + EnderContainers.__("cmd_update_found").replace("%version%", newVersion).replace("%command%", "/endc update install"));
                        updateChecked = true;
                    } else { // Nothing to do
                        p.sendMessage(Config.prefix + "§a" + EnderContainers.__("cmd_update_notfound"));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void doUpdate(final CommandSender p) {
        final String version = EnderContainers.getInstance().getDescription().getVersion();

        EnderContainers.getInstance().getServer().getScheduler().runTaskAsynchronously(EnderContainers.getInstance(), new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL(Config.updateBase + "version");
                    InputStream is = url.openStream();
                    ByteArrayOutputStream os = new ByteArrayOutputStream();
                    byte[] buf = new byte[4096];
                    int n;
                    while ((n = is.read(buf)) >= 0)
                        os.write(buf, 0, n);
                    os.close();
                    is.close();
                    byte[] data = os.toByteArray();

                    String newVersion = new String(data);

                    if (!newVersion.equalsIgnoreCase(version)) { // Do an update
                        new Updater(EnderContainers.getInstance()).update();
                        p.sendMessage(Config.prefix + "§aPlugin is now updated to the version §6" + newVersion + "§a. All new features can be found on plugin's Spigot page.");
                        p.sendMessage(Config.prefix + "§7It's recommended to reload the server to finish update. Thank you for using this plugin :D.");

                        updateChecked = false;
                    } else { // Nothing to do
                        p.sendMessage(Config.prefix + "§a" + EnderContainers.__("cmd_update_notfound"));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }


    private void viewBackups(CommandSender sender) {
        boolean mysql = EnderContainers.hasMysql();
        List<DatabaseSet> backups = null;

        if (!mysql && !EnderContainers.getConfigClass().isConfigurationSection("backups.yml", "backups")) {
            CoreUtils.errorMessage(sender, EnderContainers.__("cmd_nobackup").replace("%command%", "/endc createbackup <name>"));
            return;
        }else if(mysql){
            backups = EnderContainers.getMysqlManager().getBackups();

            if(backups == null || backups.size() == 0){
                CoreUtils.errorMessage(sender, EnderContainers.__("cmd_nobackup").replace("%command%", "/endc createbackup <name>"));
                return;
            }
        }

        sender.sendMessage("§7============§8[§6EnderContainers / Backups§8]§7============");
        sender.sendMessage(" ");

        if(!mysql) {
            for (String key : EnderContainers.getConfigClass().getConfigurationSection("backups.yml", "backups").getKeys(false)) {
                String name = EnderContainers.getConfigClass().getString("backups.yml", "backups." + key + ".name");
                String createdBy = EnderContainers.getConfigClass().getString("backups.yml", "backups." + key + ".createdBy");

                sender.sendMessage("§7 - Backup §b" + name + "§7. " + EnderContainers.__("cmd_backup_label_createdby") + " §e" + createdBy + "§7.");
            }
        }else{
            for (DatabaseSet backup : backups)
                sender.sendMessage("§7 - Backup §b" + backup.getString("name") + "§7. " + EnderContainers.__("cmd_backup_label_createdby") + " §e" + backup.getString("created_by_name") + "§7.");
        }

        sender.sendMessage(" ");
        sender.sendMessage(EnderContainers.__("cmd_backup_info").replace("%command%", "/endc backup <name>"));
    }

    private void getBackupInformation(CommandSender p, String name) {
        boolean mysql = EnderContainers.hasMysql();

        Timestamp t;
        String type;
        String path = null;
        String createdBy;

        if(!mysql) {
            String pre = "backups." + name;
            if (!EnderContainers.getConfigClass().isConfigurationSection("backups.yml", pre)) {
                CoreUtils.errorMessage(p, EnderContainers.__("cmd_backup_unknown").replace("%backup_name%", name));
                return;
            }

            t         = new Timestamp(Long.parseLong(EnderContainers.getConfigClass().getString("backups.yml", pre + ".date")));
            type      = EnderContainers.getConfigClass().getString("backups.yml", pre + ".type");
            path      = EnderContainers.getConfigClass().getString("backups.yml", pre + ".path");
            createdBy = EnderContainers.getConfigClass().getString("backups.yml", pre + ".createdBy");
        }else{
            DatabaseSet backup = EnderContainers.getMysqlManager().getBackup(name);
            if(backup == null){
                CoreUtils.errorMessage(p, EnderContainers.__("cmd_backup_unknown").replace("%backup_name%", name));
                return;
            }

            t         = backup.getTimestamp("date");
            type      = backup.getString("type");
            createdBy = backup.getString("created_by_name");
        }

        p.sendMessage("§7===========§8[§6EnderContainers / Backup Info§8]§7===========");
        p.sendMessage(" ");
        p.sendMessage("§7 - " + EnderContainers.__("cmd_backup_label_name") + ": §r" + name);
        p.sendMessage("§7 - " + EnderContainers.__("cmd_backup_label_creationdate") + ": §r" + t.toString());
        p.sendMessage("§7 - " + EnderContainers.__("cmd_backup_label_backuptype") + ": §r" + type);
        if(!mysql) p.sendMessage("§7 - Backup path: §b" + path);
        p.sendMessage("§7 - " + EnderContainers.__("cmd_backup_label_createdby") + ": §e" + createdBy);
        p.sendMessage(" ");
        p.sendMessage(EnderContainers.__("cmd_backup_label_loadcmd") + name);
        p.sendMessage(EnderContainers.__("cmd_backup_label_removecmd") + name);
        p.sendMessage(" ");
    }
}

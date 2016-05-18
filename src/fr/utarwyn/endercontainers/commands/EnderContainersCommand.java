package fr.utarwyn.endercontainers.commands;

import fr.utarwyn.endercontainers.EnderContainers;
import fr.utarwyn.endercontainers.database.DatabaseSet;
import fr.utarwyn.endercontainers.utils.Config;
import fr.utarwyn.endercontainers.utils.CoreUtils;
import fr.utarwyn.endercontainers.utils.EnderChestUtils;
import fr.utarwyn.endercontainers.utils.Updater;
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
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Timestamp;
import java.util.List;

public class EnderContainersCommand implements CommandExecutor {

    private boolean updateChecked = false;

    @SuppressWarnings("deprecation")
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!Config.enabled) {
            CoreUtils.errorMessage(sender, "Plugin is disabled.");
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
                    CoreUtils.errorMessage(p, "Usage: /ec open <player>");
                    return true;
                }
                if (!CoreUtils.playerHasPerm(p, "openchests")) {
                    CoreUtils.accessDenied(p);
                    return true;
                }

                String playername = args[1];
                Player playerToSpec = Bukkit.getPlayer(playername);

                // Check if player spectated is online or not
                if (playerToSpec == null || !playerToSpec.isOnline()) {
                    EnderChestUtils.openOfflinePlayerMainMenu(p, playername);
                    return true;
                }

                if(!p.getName().equalsIgnoreCase(playername))
                    EnderContainers.getEnderchestsManager().setLastEnderchestOpened(p, playerToSpec);

                EnderChestUtils.openPlayerMainMenu(p, playerToSpec);
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
                    CoreUtils.errorMessage(sender, "Usage: /ec createbackup <name>");
                    return true;
                } else {
                    String name = args[1];

                    sender.sendMessage(Config.prefix + "Starting backup creation...");
                    if (EnderChestUtils.createBackup(name, sender))
                        sender.sendMessage(Config.prefix + "§aBackup created with the name §b" + name + "§a.");
                    else
                        CoreUtils.errorMessage(sender, "Backup §6" + name + " §calready exists. Please change name.");
                }
            } else if (args[0].equalsIgnoreCase("loadbackup")) {
                if (!CoreUtils.senderHasPerm(sender, "backups.load")) {
                    CoreUtils.accessDenied(sender);
                    return true;
                }
                if (args.length < 2) {
                    CoreUtils.errorMessage(sender, "Usage: /ec loadbackup <name>");
                    return true;
                } else {
                    String name = args[1];

                    sender.sendMessage(Config.prefix + "Starting backup loading...");
                    sender.sendMessage(Config.prefix + "§cDisable plugin....");

                    EnderChestUtils.loadBackup(name, sender);
                }
            } else if (args[0].equalsIgnoreCase("backup")) {
                if (!CoreUtils.senderHasPerm(sender, "backups.info")) {
                    CoreUtils.accessDenied(sender);
                    return true;
                }
                if (args.length < 2) {
                    CoreUtils.errorMessage(sender, "Usage: /ec backup <name>");
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
                    CoreUtils.errorMessage(sender, "Usage: /ec rmbackup <name>");
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
                            CoreUtils.errorMessage(sender, "To install an update, do : §6/ec update §cto check for updates.");
                            return true;
                        } else {
                            doUpdate(sender);
                        }
                    } else {
                        CoreUtils.errorMessage(sender, "Usage: /ec update [install]");
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
                sender.sendMessage(Config.prefix + "§aConfiguration reloaded !");
            } else {
                sender.sendMessage(Config.prefix + "§cUnknown command : §6/endercontainers help§c.");
            }
        } else {
            sender.sendMessage(Config.prefix + "Created by §3Utarwyn§7. Version §e" + EnderContainers.getInstance().getDescription().getVersion() + "§7.");
            sender.sendMessage(Config.prefix + "To show plugin's help: §6/endercontainers help§7.");
        }

        return true;
    }


    public void getHelp(CommandSender p, int page) {
        int maxpage = 2;
        if (maxpage < page) page = maxpage;

        p.sendMessage(" ");
        p.sendMessage("§7 ==============§8 [§6EnderContainers§r | §9" + page + "/" + maxpage + "§8] §7============== ");
        p.sendMessage(" ");

        if (page == 1) {
            sendFormattedHelpLine(p, "Open your enderchest", "§e/enderchest [number]", "*");
            sendFormattedHelpLine(p, "Open an enderchest", "§b/ec open <player>", "backups.openchests");

            p.sendMessage(" ");

            sendFormattedHelpLine(p, "List backups", "§b/ec backups", "backups.view");
            sendFormattedHelpLine(p, "Create a backup", "§b/ec createbackup <name>", "backups.create");
            sendFormattedHelpLine(p, "Show backup information", "§b/ec backup <name>", "backups.info");
        } else if (page == 2) {
            sendFormattedHelpLine(p, "Load a backup (indefinitly)", "§b/ec loadbackup <name>", "backups.load");
            sendFormattedHelpLine(p, "Remove a backup", "§b/ec rmbackup <name>", "backups.remove");

            p.sendMessage(" ");

            sendFormattedHelpLine(p, "View config", "§b/ec config", null, true);
            sendFormattedHelpLine(p, "Check for updates", "§e/ec update", "update");
            sendFormattedHelpLine(p, "Reload plugin configuration", "§e/ec reload", null, true);
        }

        p.sendMessage(" ");
    }
    public void sendFormattedHelpLine(CommandSender sender, String title, String command, String perm){
        sendFormattedHelpLine(sender, title, command, perm, false);
    }
    public void sendFormattedHelpLine(CommandSender sender, String title, String command, String perm, Boolean isOp){
        String message       = "§7 - " + title + " : ";
        String hiddenCommand = "§c§k";

        for(int i = 0; i < command.length(); i++) hiddenCommand += "-";

        if((perm != null && perm.equals("*")) || (perm != null && CoreUtils.senderHasPerm(sender, perm)) || (isOp && (sender instanceof Player) && sender.isOp()) || sender instanceof ConsoleCommandSender)
            message += command;
        else
            message += hiddenCommand;

        sender.sendMessage(message);
    }


    public void getStringConfig(CommandSender p) {
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
        String rAdc = "§cfalse";
        if (Config.allowDoubleChest) rAdc = "§atrue";

        p.sendMessage("§7 - Enabled = " + rEna);
        p.sendMessage("§7 - Debug = " + rDeb);
        p.sendMessage("§7 - PluginPrefix = §r" + ChatColor.stripColor(Config.prefix));

        p.sendMessage(" ");

        p.sendMessage("§7 - SaveDir = §r" + Config.saveDir);
        p.sendMessage("§7 - MaxEnderchests = §r" + Config.maxEnderchests);
        p.sendMessage("§7 - DefaultEnderchestsNumber = §r" + Config.defaultEnderchestsNumber);
        p.sendMessage("§7 - AllowDoubleChest = " + rAdc);

        p.sendMessage(" ");

        p.sendMessage("§7 - EnderchestOpenPerm = §r" + Config.enderchestOpenPerm);
        p.sendMessage("§7 - MainEnderchestTitle = §r" + Config.mainEnderchestTitle);
        p.sendMessage("§7 - EnderchestTitle = §r" + Config.enderchestTitle);
    }

    public void checkForUpdate(final CommandSender p) {
        final String version = EnderContainers.getInstance().getDescription().getVersion();

        EnderContainers.getInstance().getServer().getScheduler().runTaskAsynchronously(EnderContainers.getInstance(), new Runnable() {
            @Override
            public void run() {
                String newVersion = "-1";

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

                    newVersion = new String(data);

                    if (!newVersion.equalsIgnoreCase(version)) { // Do an update
                        p.sendMessage(Config.prefix + "§7Update found : v§e" + newVersion + "§7. To do this update : §6/ec update install");
                        updateChecked = true;
                    } else { // Nothing to do
                        p.sendMessage(Config.prefix + "§aThere is no update at this time. Retry later.");
                    }
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void doUpdate(final CommandSender p) {
        final String version = EnderContainers.getInstance().getDescription().getVersion();

        EnderContainers.getInstance().getServer().getScheduler().runTaskAsynchronously(EnderContainers.getInstance(), new Runnable() {
            @Override
            public void run() {
                String newVersion = "-1";

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

                    newVersion = new String(data);

                    if (!newVersion.equalsIgnoreCase(version)) { // Do an update
                        new Updater(EnderContainers.getInstance()).update();
                        p.sendMessage(Config.prefix + "§aPlugin is now updated to the version §6" + newVersion + "§a. All new features can be found on plugin's Spigot page.");
                        p.sendMessage(Config.prefix + "§7It's recommended to reload the server to finish update. Thank you for using this plugin :D.");

                        updateChecked = false;
                    } else { // Nothing to do
                        p.sendMessage(Config.prefix + "§aThere is no update at this time. Retry later.");
                    }
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }


    public void viewBackups(CommandSender sender) {
        boolean mysql = EnderContainers.hasMysql();
        List<DatabaseSet> backups = null;

        if (!mysql && !EnderContainers.getConfigClass().isConfigurationSection("backups.yml", "backups")) {
            CoreUtils.errorMessage(sender, "No backup. Create backup : §6/ec createbackup <name>");
            return;
        }else if(mysql){
            backups = EnderContainers.getMysqlManager().getBackups();

            if(backups == null || backups.size() == 0){
                CoreUtils.errorMessage(sender, "No backup. Create backup : §6/ec createbackup <name>");
                return;
            }
        }

        sender.sendMessage("§7============§8[§6EnderContainers / Backups§8]§7============");
        sender.sendMessage(" ");

        if(!mysql) {
            for (String key : EnderContainers.getConfigClass().getConfigurationSection("backups.yml", "backups").getKeys(false)) {
                String name = EnderContainers.getConfigClass().getString("backups.yml", "backups." + key + ".name");
                String createdBy = EnderContainers.getConfigClass().getString("backups.yml", "backups." + key + ".createdBy");

                sender.sendMessage("§7 - Backup §b" + name + "§7. Created by §e" + createdBy + "§7.");
            }
        }else{
            for (DatabaseSet backup : backups)
                sender.sendMessage("§7 - Backup §b" + backup.getString("name") + "§7. Created by §e" + backup.getString("created_by_name") + "§7.");
        }

        sender.sendMessage(" ");
        sender.sendMessage("§7 To show information about a backup: §c/ec backup <name>§7.");
    }

    public void getBackupInformation(CommandSender p, String name) {
        boolean mysql = EnderContainers.hasMysql();

        Timestamp t;
        String type;
        String path = null;
        String createdBy;

        if(!mysql) {
            String pre = "backups." + name;
            if (!EnderContainers.getConfigClass().isConfigurationSection("backups.yml", pre)) {
                CoreUtils.errorMessage(p, "Backup §6" + name + "§c is undefined.");
                return;
            }

            t         = new Timestamp(Long.parseLong(EnderContainers.getConfigClass().getString("backups.yml", pre + ".date")));
            type      = EnderContainers.getConfigClass().getString("backups.yml", pre + ".type");
            path      = EnderContainers.getConfigClass().getString("backups.yml", pre + ".path");
            createdBy = EnderContainers.getConfigClass().getString("backups.yml", pre + ".createdBy");
        }else{
            DatabaseSet backup = EnderContainers.getMysqlManager().getBackup(name);
            if(backup == null){
                CoreUtils.errorMessage(p, "Backup §6" + name + "§c is undefined.");
                return;
            }

            t         = backup.getTimestamp("date");
            type      = backup.getString("type");
            createdBy = backup.getString("created_by_name");
        }

        p.sendMessage("§7===========§8[§6EnderContainers / Backup Info§8]§7===========");
        p.sendMessage(" ");
        p.sendMessage("§7 - Name: §r" + name);
        p.sendMessage("§7 - Date creation: §r" + t.toString());
        p.sendMessage("§7 - Backup type: §r" + type);
        if(!mysql) p.sendMessage("§7 - Backup path: §b" + path);
        p.sendMessage("§7 - Created by: §e" + createdBy);
        p.sendMessage(" ");
        p.sendMessage("§8 >> To load this backup: §c/ec loadbackup " + name);
        p.sendMessage("§8 >> To remove this backup: §c/ec rmbackup " + name);
        p.sendMessage(" ");
    }
}

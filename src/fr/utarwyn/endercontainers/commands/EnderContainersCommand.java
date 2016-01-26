package fr.utarwyn.endercontainers.commands;

import fr.utarwyn.endercontainers.EnderContainers;
import fr.utarwyn.endercontainers.managers.EnderchestsManager;
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
import org.bukkit.entity.Player;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Timestamp;

public class EnderContainersCommand implements CommandExecutor {

    private boolean updateChecked = false;

    @SuppressWarnings("deprecation")
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) return false;
        Player p = (Player) sender;

        if (!Config.enabled) {
            CoreUtils.errorMessage(p, "Plugin is disabled.");
            return true;
        }

        if (args.length >= 1) {
            if (args[0].equalsIgnoreCase("help")) {
                if (args.length < 2) {
                    getHelp(p, 1);
                } else {
                    if (StringUtils.isNumeric(args[1]))
                        getHelp(p, Integer.parseInt(args[1]));
                    else
                        getHelp(p, 0);
                }
            } else if (args[0].equalsIgnoreCase("open")) {
                if (args.length <= 1) {
                    CoreUtils.errorMessage(p, "Usage: /ec open <player>");
                    return true;
                }
                String playername = args[1];
                Player playerToSpec = Bukkit.getPlayer(playername);

                // Check if player spectated is online or not
                if (playerToSpec == null || !playerToSpec.isOnline()) {
                    CoreUtils.errorMessage(p, "Player §6" + playername + " §cis disconnected. Please retry later.");
                    return true;
                }

                if(!p.getName().equalsIgnoreCase(playername))
                    EnderContainers.getEnderchestsManager().setLastEnderchestOpened(p, playerToSpec);

                EnderChestUtils.openPlayerMainMenu(p, playerToSpec);
            } else if (args[0].equalsIgnoreCase("backups")) {
                if (!CoreUtils.playerHasPerm(p, "backups.view")) {
                    CoreUtils.accessDenied(p);
                    return true;
                }
                viewBackups(p);
            } else if (args[0].equalsIgnoreCase("createbackup")) {
                if (!CoreUtils.playerHasPerm(p, "backups.create")) {
                    CoreUtils.accessDenied(p);
                    return true;
                }
                if (args.length < 2) {
                    CoreUtils.errorMessage(p, "Usage: /ec createbackup <name>");
                    return true;
                } else {
                    String name = args[1];

                    p.sendMessage(Config.prefix + "Starting backup creation...");
                    if (EnderChestUtils.createBackup(name, p))
                        p.sendMessage(Config.prefix + "§aBackup created with the name §b" + name + "§a.");
                    else
                        CoreUtils.errorMessage(p, "Backup §6" + name + " §calready exist. Please change name.");
                }
            } else if (args[0].equalsIgnoreCase("loadbackup")) {
                if (!CoreUtils.playerHasPerm(p, "backups.load")) {
                    CoreUtils.accessDenied(p);
                    return true;
                }
                if (args.length < 2) {
                    CoreUtils.errorMessage(p, "Usage: /ec loadbackup <name>");
                    return true;
                } else {
                    String name = args[1];

                    p.sendMessage(Config.prefix + "Starting backup loading...");
                    p.sendMessage(Config.prefix + "§cDisable plugin....");

                    EnderChestUtils.loadBackup(name, p);
                }
            } else if (args[0].equalsIgnoreCase("backup")) {
                if (!CoreUtils.playerHasPerm(p, "backups.info")) {
                    CoreUtils.accessDenied(p);
                    return true;
                }
                if (args.length < 2) {
                    CoreUtils.errorMessage(p, "Usage: /ec backup <name>");
                    return true;
                } else {
                    String name = args[1];
                    getBackupInformation(p, name);
                }
            } else if (args[0].equalsIgnoreCase("rmbackup")) {
                if (!CoreUtils.playerHasPerm(p, "backups.remove")) {
                    CoreUtils.accessDenied(p);
                    return true;
                }
                if (args.length < 2) {
                    CoreUtils.errorMessage(p, "Usage: /ec rmbackup <name>");
                    return true;
                } else {
                    String name = args[1];
                    EnderChestUtils.removeBackup(p, name);
                }
            } else if (args[0].equalsIgnoreCase("config")) {
                getStringConfig(p);
            } else if (args[0].equalsIgnoreCase("update")) {
                if (!CoreUtils.playerHasPerm(p, "update")) {
                    CoreUtils.accessDenied(p);
                    return true;
                }
                if (args.length >= 2) {
                    if (args[1].equalsIgnoreCase("install")) {
                        if (!updateChecked) {
                            CoreUtils.errorMessage(p, "To install an update, do : §6/ec update §cto check for updates.");
                            return true;
                        } else {
                            doUpdate(p);
                        }
                    } else {
                        CoreUtils.errorMessage(p, "Usage: /ec update [install]");
                        return true;
                    }
                } else {
                    checkForUpdate(p);
                }
            } else if (args[0].equalsIgnoreCase("reload")) {
                if (!p.isOp()) {
                    CoreUtils.accessDenied(p);
                    return true;
                }
                EnderContainers.getInstance().reloadConfiguration();
                p.sendMessage(Config.prefix + "§aConfiguration reloaded !");
            } else {
                p.sendMessage(Config.prefix + "§cUnknow command : §6/endercontainers help§c.");
            }
        } else {
            p.sendMessage(Config.prefix + "Created by §3Utarwyn§7. Version §e" + EnderContainers.getInstance().getDescription().getVersion() + "§7.");
            p.sendMessage(Config.prefix + "To view plugin help: §6/endercontainers help§7.");
        }

        return true;
    }


    public void getHelp(Player p, int page) {
        int maxpage = 2;
        if (maxpage < page) page = maxpage;
        p.sendMessage("§7==============§8[§6EnderContainers§r - §9" + page + "/" + maxpage + "§8]§7==============");
        p.sendMessage(" ");
        if (page == 1) {
            p.sendMessage("§7 - Open an enderchest : §c/ec open <player> <number>");
            p.sendMessage(" ");
            p.sendMessage("§7 - List backups : §c/ec backups");
            p.sendMessage("§7 - Create a backup : §c/ec createbackup <name> [player]");
            p.sendMessage("§7 - View backup information : §c/ec backup <name>");
            p.sendMessage("§7 - Load a backup (indefinitly) : §c/ec loadbackup <name>");
            p.sendMessage("§7 - Remove a backup (indefinitly) : §c/ec rmbackup <name>");
        } else if (page == 2) {
            p.sendMessage("§7 - View config : §c/ec config");
            p.sendMessage("§7 - Check for updates : §c/ec update");
            p.sendMessage("§7 - Reload plugin configuration : §c/ec reload");
        }
        p.sendMessage(" ");
    }


    public void getStringConfig(Player p) {
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

        p.sendMessage("§7 - Enabled=" + rEna);
        p.sendMessage("§7 - Debug=" + rDeb);
        p.sendMessage("§7 - PluginPrefix=§r" + ChatColor.stripColor(Config.prefix));

        p.sendMessage(" ");

        p.sendMessage("§7 - SaveDir=§r" + Config.saveDir);
        p.sendMessage("§7 - MaxEnderchests=§r" + Config.maxEnderchests);
        p.sendMessage("§7 - DefaultEnderchestsNumber=§r" + Config.defaultEnderchestsNumber);

        p.sendMessage(" ");

        p.sendMessage("§7 - EnderchestOpenPerm=§r" + Config.enderchestOpenPerm);
        p.sendMessage("§7 - MainEnderchestTitle=§r" + Config.mainEnderchestTitle);
        p.sendMessage("§7 - EnderchestTitle=§r" + Config.enderchestTitle);
    }

    public void checkForUpdate(final Player p) {
        final String version = EnderContainers.getInstance().getDescription().getVersion();

        EnderContainers.getInstance().getServer().getScheduler().runTaskAsynchronously(EnderContainers.getInstance(), new Runnable() {
            @Override
            public void run() {
                String newVersion = "-1";

                try {
                    URL url = new URL("http://185.13.38.245/plugins/EnderContainers/lastestVersion.txt");
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

    public void doUpdate(final Player p) {
        final String version = EnderContainers.getInstance().getDescription().getVersion();

        EnderContainers.getInstance().getServer().getScheduler().runTaskAsynchronously(EnderContainers.getInstance(), new Runnable() {
            @Override
            public void run() {
                String newVersion = "-1";

                try {
                    URL url = new URL("http://185.13.38.245/plugins/EnderContainers/lastestVersion.txt");
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


    public void viewBackups(Player p) {
        if (!EnderContainers.getConfigClass().isConfigurationSection("backups.yml", "backups")) {
            CoreUtils.errorMessage(p, "No backup. Create backup : §6/ec createbackup <name>");
            return;
        }

        p.sendMessage("§7============§8[§6EnderContainers / Backups§8]§7============");
        p.sendMessage(" ");
        for (String key : EnderContainers.getConfigClass().getConfigurationSection("backups.yml", "backups").getKeys(false)) {
            String name = EnderContainers.getConfigClass().getString("backups.yml", "backups." + key + ".name");
            long date = Long.parseLong(EnderContainers.getConfigClass().getString("backups.yml", "backups." + key + ".date"));
            String createdBy = EnderContainers.getConfigClass().getString("backups.yml", "backups." + key + ".createdBy");

            p.sendMessage("§7 - Backup §b" + name + "§7. Created by §e" + createdBy + "§7 (" + new Timestamp(date).toString() + ").");
        }
        p.sendMessage(" ");
    }

    public void getBackupInformation(Player p, String name) {
        String pre = "backups." + name;
        if (!EnderContainers.getConfigClass().isConfigurationSection("backups.yml", pre)) {
            CoreUtils.errorMessage(p, "Backup §6" + name + "§c is undefined.");
            return;
        }

        Timestamp t = new Timestamp(Long.parseLong(EnderContainers.getConfigClass().getString("backups.yml", pre + ".date")));

        p.sendMessage("§7===========§8[§6EnderContainers / Backup Info§8]§7===========");
        p.sendMessage(" ");
        p.sendMessage("§7 - Name: §r" + name);
        p.sendMessage("§7 - Date creation: §r" + t.toString());
        p.sendMessage("§7 - Backup type: §r" + EnderContainers.getConfigClass().getString("backups.yml", pre + ".type"));
        p.sendMessage("§7 - Backup directory: §b" + EnderContainers.getConfigClass().getString("backups.yml", pre + ".directory"));
        p.sendMessage(" ");
        p.sendMessage("§8 >> To load this backup: §c/ec loadbackup " + name);
        p.sendMessage(" ");
    }
}

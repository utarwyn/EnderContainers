package fr.utarwyn.endercontainers.utils;

import fr.utarwyn.endercontainers.EnderContainers;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class CoreUtils {

    public static void log(String message, boolean force) {
        if (Config.debug || force) Bukkit.getConsoleSender().sendMessage(message);
    }
    public static void log(String message) {
        log(message, false);
    }

    public static void error(String message) {
        Bukkit.getConsoleSender().sendMessage(Config.prefix + "§4" + message);
    }


    public static void errorMessage(CommandSender sender, String message) {
        sender.sendMessage(Config.prefix + "§c" + message);
    }

    public static void accessDenied(CommandSender sender) {
        sender.sendMessage(Config.prefix + "§c" + EnderContainers.__("error_player_denied"));
    }
    public static void consoleDenied(CommandSender sender){
        errorMessage(sender, EnderContainers.__("error_console_denied"));
    }

    public static boolean playerHasPerm(Player player, String perm) {
        return player.hasPermission("endercontainers." + perm) || player.isOp();
    }
    public static boolean senderHasPerm(CommandSender sender, String perm){
        return !(sender instanceof Player && !playerHasPerm((Player) sender, perm)) || sender instanceof ConsoleCommandSender;
    }

    public static String replacePlayerName(String base, Player player) {
        return replacePlayerName(base, player.getName());
    }
    public static String replacePlayerName(String base, String playername) {
        String r = "";

        if (base.contains("%player%"))
            r = base.replace("%player%", playername);
        else
            r = base;

        return r;
    }
    public static String replaceEnderchestNum(String base, Integer num, String playername) {
        String r = replacePlayerName(base, playername);

        if (base.contains("%num%"))
            r = r.replace("%num%", num.toString());

        return r;
    }

    public static int getInventorySize(Inventory inv) {
        int r = 0;
        for (ItemStack i : inv.getContents()) if (i != null) r++;
        return r;
    }
    public static boolean soundExists(String soundName) {

        for (Sound sound : Sound.values()) {
            if (sound.name().equals(soundName)) {
                return true;
            }
        }

        return false;
    }
    public static void playSoundTo(String soundName, Player player){
        if(soundExists(soundName))
            player.playSound(player.getLocation(), Sound.valueOf(soundName), 1F, 1F);
        else
            CoreUtils.log("§cThe sound §6" + soundName + "§c doesn't exists. Please change it in the config.", true);
    }

    public static String getServerVersion(){
        String packageName = EnderContainers.getInstance().getServer().getClass().getPackage().getName();
        return packageName.substring(packageName.lastIndexOf('.') + 1);
    }

    /*
    public static InputStream getDefaultConfiguration(){
        return EnderContainers.getInstance().getClass().getResourceAsStream("/ressources/config.default.yml");
    }
    public static void beautifyConfig(){
        ConfigClass cc = EnderContainers.getConfigClass();
        String configVersion = cc.contains("main", "saveVersion") ? cc.getString("main", "saveVersion") : "-1";

        // Check if the action is needed
        if(configVersion.equals(EnderContainers.getInstance().getDescription().getVersion())) return;
        System.out.println("Run config beautifier...");

        BufferedReader reader = null; BufferedWriter writer = null;

        try {
            reader = new BufferedReader(new InputStreamReader(getDefaultConfiguration()));
            writer = new BufferedWriter(new FileWriter(new File(EnderContainers.getInstance().getDataFolder().toString(), "config.yml")));

            writer.write("");

            for(String line; (line = reader.readLine()) != null;){
                writer.write(line);
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
                if (writer != null) {
                    writer.close();
                }

                // refreshConfig();
            } catch(Exception e){
                e.printStackTrace();
            }
        }
    }
    private static void refreshConfig(){
        ConfigClass cc = EnderContainers.getConfigClass();

        cc.setAutoSaving = false;

        cc.set("main", "enabled", Config.enabled);
        cc.set("main", "debug", Config.debug);
        cc.set("main", "prefix", Config.prefix);
        cc.set("main", "locale", Config.pluginLocale);
        cc.set("main", "disabledWorlds", Config.disabledWorlds);

        cc.set("main", "enderchests.max", Config.maxEnderchests);
        cc.set("main", "enderchests.default", Config.defaultEnderchestsNumber);

        cc.set("main", "mysql.enabled", Config.mysql);
        cc.set("main", "mysql.host", Config.DB_HOST);
        cc.set("main", "mysql.port", Config.DB_PORT);
        cc.set("main", "mysql.user", Config.DB_USER);
        cc.set("main", "mysql.password", Config.DB_PASS);
        cc.set("main", "mysql.database", Config.DB_BDD);
        cc.set("main", "mysql.tablePrefix", Config.DB_PREFIX);

        cc.set("main", "others.blocknametag", Config.blockNametag);
        cc.set("main", "others.openingChestSound", Config.openingChestSound);
        cc.set("main", "others.closingChestSound", Config.closingChestSound);
        cc.set("main", "others.updateChecker", Config.updateChecker);

        cc.setAutoSaving = true;
        cc.saveConfig("main");
    }
    */
}
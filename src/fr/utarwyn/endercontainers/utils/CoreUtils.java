package fr.utarwyn.endercontainers.utils;

import fr.utarwyn.endercontainers.EnderContainers;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
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
        return !(sender instanceof Player && !playerHasPerm((Player) sender, perm)) || !(sender instanceof Player);
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
    public static String replaceEnderchestNum(String base, Integer num, Player player) {
        return replaceEnderchestNum(base, num, player.getName());
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
}
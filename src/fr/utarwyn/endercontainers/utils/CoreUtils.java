package fr.utarwyn.endercontainers.utils;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class CoreUtils {

    public static void log(String message) {
        if (Config.debug) Bukkit.getConsoleSender().sendMessage(message);
    }

    public static void error(String message) {
        Bukkit.getConsoleSender().sendMessage("§f[§4Error§f] §4" + message);
    }


    public static void errorMessage(Player player, String message) {
        player.sendMessage(Config.prefix + "§c" + message);
    }

    public static void accessDenied(Player player) {
        player.sendMessage(Config.prefix + "§c" + "You don't have access to this command.");
    }

    public static boolean playerHasPerm(Player player, String perm) {
        return player.hasPermission("endercontainers." + perm);
    }


    public static String replacePlayerName(String base, Player player) {
        String r = "";

        if (base.contains("%player%"))
            r = base.replace("%player%", player.getName());
        else
            r = base;

        return r;
    }

    public static String replaceEnderchestNum(String base, Integer num, Player player) {
        String r = replacePlayerName(base, player);

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
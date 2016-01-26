package fr.utarwyn.endercontainers.utils;

import org.bukkit.entity.Player;

public class PluginMsg {

    public static void doesNotHavePerm(Player p) {
        CoreUtils.errorMessage(p, "You don't have the permission to do this.");
    }

    public static void cantUseHereFaction(Player p) {
        CoreUtils.errorMessage(p, "You can't use this EnderChest here.");
    }

    public static void cannotOpenEnderchest(Player p) {
        CoreUtils.errorMessage(p, "You can't open this EnderChest !");
    }

    public static void enderchestUnknown(Player p, Integer index) {
        CoreUtils.errorMessage(p, "Enderchest " + index + " doesn't exist.");
    }
}

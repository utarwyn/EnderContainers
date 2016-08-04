package fr.utarwyn.endercontainers.utils;

import fr.utarwyn.endercontainers.EnderContainers;
import org.bukkit.entity.Player;

public class PluginMsg {

    public static void doesNotHavePerm(Player p) {
        CoreUtils.errorMessage(p, "You don't have the permission to do this.");
    }

    public static void cantUseHereFaction(Player p) {
        CoreUtils.errorMessage(p, EnderContainers.__("error_access_denied_factions"));
    }

    public static void cannotOpenEnderchest(Player p) {
        CoreUtils.errorMessage(p, EnderContainers.__("error_cannot_open_enderchest"));
    }

    public static void enderchestUnknown(Player p, Integer index) {
        CoreUtils.errorMessage(p, EnderContainers.__("error_unknown_enderchest").replace("%enderchest%", Integer.toString(index)));
    }

    public static void enderchestEmpty(Player p) {
        CoreUtils.errorMessage(p, EnderContainers.__("enderchest_empty"));
    }

    public static void pluginDisabled(Player p){
        CoreUtils.errorMessage(p, EnderContainers.__("error_plugin_disabled"));
    }
}

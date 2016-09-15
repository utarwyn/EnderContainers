package fr.utarwyn.endercontainers.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Config {

    /**
     * General configuration
     */
    public static boolean enabled = true;
    public static boolean debug = false;
    public static String prefix = "§8[§6EnderContainers§8] §7";
    public static List<String> disabledWorlds = new ArrayList<>();
    public static String pluginPrefix = prefix + "";
    public static String updateBase = "http://utarwyn.xyz/plugins/endercontainers/";

    public static String pluginLocale = "en";
    public static List<String> dependencies = Arrays.asList("Factions", "PlotSquared", "Citizens");


    /**
     * Save configuration
     */
    public static String saveDir = "enderchests/"; // End with / !


    /**
     * Enderchests configuration
     */
    public static Integer defaultEnderchestsNumber = 1;

    public static String enderchestOpenPerm = "endercontainers.slot.";
    public static Integer maxEnderchests = 27;


    /**
     * Database configuration
     */
    public static boolean mysql = false;

    public static String DB_HOST   = "localhost";
    public static Integer DB_PORT  = 3306;
    public static String DB_USER   = "root";
    public static String DB_PASS   = "";
    public static String DB_BDD    = "default";
    public static String DB_PREFIX = "ec_";


    /**
     * Fun & other configuration
     */
    public static boolean blockNametag     = false;
    public static String openingChestSound = "CHEST_OPEN";
    public static String closingChestSound = "CHEST_CLOSE";
    public static boolean updateChecker    = true;

    public static int refreshTimeout = 60;
}

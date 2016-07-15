package fr.utarwyn.endercontainers.utils;

public class Config {

    /**
     * ----- General configuration
     * This is the general configuration
     */
    public static boolean enabled = true;
    public static boolean debug = false;
    public static String prefix = "§8[§6EnderContainers§8] §7";
    public static String pluginPrefix = prefix + "";
    public static String updateBase = "http://utarwyn.xyz/plugins/endercontainers/";

    public static String pluginLocale = "en";


    /**
     * ----- Save configuration
     * This is the save configuration
     */
    public static String saveDir = "enderchests/"; // End with / !


    /**
     * ----- Enderchests configuration
     * This is the Enderchests configuration
     */
    public static Integer defaultEnderchestsNumber = 1;

    public static String enderchestOpenPerm = "endercontainers.slot.";
    public static Integer maxEnderchests = 27;


    /**
     * ----- Database configuration
     * This is the database configuration
     */
    public static boolean mysql = false;

    public static String DB_HOST   = "localhost";
    public static Integer DB_PORT  = 3306;
    public static String DB_USER   = "root";
    public static String DB_PASS   = "";
    public static String DB_BDD    = "default";
    public static String DB_PREFIX = "ec_";

    /**
     * ----- Dependencies configuration
     * This is the dependencies configuration
     */
    public static boolean factionsSupport = true;
    public static boolean plotSquaredSupport = true;
    public static boolean townySupport = true;
    public static boolean citizensSupport = true;

    /**
     * ----- Fun & others configuration
     * This is the funnies & others configuration
     */
    public static boolean blockNametag     = false;
    public static String openingChestSound = "CHEST_OPEN";
    public static String closingChestSound = "CHEST_CLOSE";
    public static boolean updateChecker    = true;
}

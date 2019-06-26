package fr.utarwyn.endercontainers.util;

import fr.utarwyn.endercontainers.EnderContainers;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class used to check if there is any update for the plugin.
 *
 * @author Utarwyn
 * @since 2.0.0
 */
public class Updater {

    /**
     * Stores the Spigot ressource ID to check for update
     */
    private static final int RESSOURCE_ID = 4750;
    /**
     * URL used to get the current version of the plugin
     */
    private static final String VERSION_URL = "https://api.spiget.org/v2/resources/" + RESSOURCE_ID + "/versions?size=1&sort=-releaseDate";
    /**
     * URL used to get the last update description of the plugin
     */
    private static final String DESCRIPTION_URL = "https://api.spiget.org/v2/resources/" + RESSOURCE_ID + "/updates?size=1&sort=-date";
    /**
     * Stores the instance of the Updater class (Singleton class)
     */
    private static Updater instance;
    /**
     * Stores if the plugin is up to date or not.
     */
    private boolean upToDate;

    /**
     * Stores the last version of the plugin
     */
    private String newestVersion;

    /**
     * Stores the last update title of the plugin (from Spigot)
     */
    private String updateTitle;

    /**
     * Utility class!
     */
    private Updater() {
        this.upToDate = true;
        checkForUpdate();
    }

    /**
     * Gets the instance of the Updater
     *
     * @return Instance of this manager
     */
    public static Updater getInstance() {
        if (instance == null) {
            instance = new Updater();
        }

        return instance;
    }

    /**
     * Returns if the plugin is up to date or not.
     *
     * @return True if the plugin is up to date otherwise false
     */
    public boolean isUpToDate() {
        return this.upToDate;
    }

    /**
     * Returns the newest version of the plugin
     *
     * @return Newest version of EnderContainers
     */
    public String getNewestVersion() {
        return this.newestVersion;
    }

    /**
     * Notify to the console if the plugin have to be updated or not.
     */
    public void notifyUpToDate() {
        Logger logger = EnderContainers.getInstance().getLogger();

        if (this.upToDate) {
            logger.info("You are using the newest version of the plugin (" + this.getLocalVersion() + ").");
        } else {
            logger.warning("  ");
            logger.warning("-----------[Plugin Update!]---------");
            logger.warning("    Your version: " + this.getLocalVersion());
            logger.warning("    Newest version: " + this.newestVersion);
            logger.warning("  ");
            logger.warning("    Update message: " + this.updateTitle);
            logger.warning("------------------------------------");
        }
    }

    /**
     * Start the checking of a new version of the plugin
     */
    private void checkForUpdate() {
        try {
            JSONArray versionsArray = (JSONArray) JSONValue.parseWithException(new BufferedReader(new InputStreamReader(new URL(VERSION_URL).openStream())));
            String lastVersion = ((JSONObject) versionsArray.get(versionsArray.size() - 1)).get("name").toString();

            this.upToDate = !this.compareVersion(this.getLocalVersion(), lastVersion);
            this.newestVersion = lastVersion;

            if (!this.upToDate) {
                this.searchUpdateTitle();
            }
        } catch (ParseException | IOException e) {
            EnderContainers.getInstance().getLogger().log(Level.SEVERE, "Cannot retrieve the local version of the plugin", e);
        }
    }

    /**
     * Search for the last update title
     */
    private void searchUpdateTitle() {
        try {
            JSONArray versionsArray = (JSONArray) JSONValue.parseWithException(new BufferedReader(new InputStreamReader(new URL(DESCRIPTION_URL).openStream())));

            this.updateTitle = ((JSONObject) versionsArray.get(versionsArray.size() - 1)).get("title").toString();
        } catch (ParseException | IOException e) {
            EnderContainers.getInstance().getLogger().log(Level.SEVERE, "Cannot retrieve the title of the latest update", e);
        }
    }

    /**
     * Compare two versions (locale and remote)
     *
     * @param local  Local version of the plugin
     * @param remote Remote version of the plugin
     * @return True if the local version is before the remote version
     */
    private boolean compareVersion(String local, String remote) {
        // Parse the version to only keep the integer.
        int localVer = Integer.parseInt(local.replaceAll("[^\\d]", "")) * 100;
        int remoteVer = Integer.parseInt(remote.replaceAll("[^\\d]", "")) * 100;

        // Manage sub-version character at the end of the version.
        char localSubVer = local.charAt(local.length() - 1);
        char remoteSubVer = remote.charAt(remote.length() - 1);

        if (localSubVer >= 'a' && localSubVer <= 'z')
            localVer += (localSubVer - 'a') + 1;
        if (remoteSubVer >= 'a' && remoteSubVer <= 'z')
            remoteVer += (remoteSubVer - 'a') + 1;

        // Compare the two versions!
        return localVer < remoteVer;
    }

    /**
     * Return the local version of EnderContainers. Only a shortcut.
     *
     * @return Local version of the plugin.
     */
    private String getLocalVersion() {
        return EnderContainers.getInstance().getDescription().getVersion();
    }

}

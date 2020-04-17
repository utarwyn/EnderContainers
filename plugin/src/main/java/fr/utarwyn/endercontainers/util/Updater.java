package fr.utarwyn.endercontainers.util;

import com.google.gson.Gson;
import fr.utarwyn.endercontainers.AbstractManager;
import fr.utarwyn.endercontainers.configuration.Files;
import org.bukkit.command.CommandSender;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import static fr.utarwyn.endercontainers.EnderContainers.PREFIX;

/**
 * Class used to check if there is any update for the plugin.
 *
 * @author Utarwyn
 * @since 2.0.0
 */
public class Updater extends AbstractManager implements Runnable {

    /**
     * URL to download the plugin
     */
    private static final String DOWNLOAD_LINK = "https://bit.ly/2A8Xv8S";

    /**
     * URL to go to the plugin's official wiki
     */
    private static final String WIKI_LINK = "https://bit.ly/2D07g9a";

    /**
     * URL used to get the current version of the plugin
     */
    private static final String VERSION_URL = "https://api.spiget.org/v2/resources/4750/versions?size=1&sort=-releaseDate";

    /**
     * Decimal value of the current version of the plugin
     */
    private long currentVersionValue;

    /**
     * Decimal value of the latest version of the plugin
     */
    private long latestVersionValue;

    /**
     * Current version of the plugin
     */
    private String currentVersion;

    /**
     * Latest version of the plugin
     */
    private String latestVersion;

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void load() {
        // Check for updates if enabled by the server administrator
        if (Files.getConfiguration().isUpdateChecker()) {
            this.plugin.getServer().getScheduler().runTask(this.plugin, this);
        } else {
            this.plugin.getLogger().warning("You have disabled update checking. Please be sure that the plugin is up to date.");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void unload() {
        this.latestVersion = null;
        this.currentVersion = null;
        this.latestVersionValue = 0;
        this.currentVersionValue = 0;
    }

    /**
     * Notify a command sender if the plugin needs to be updated.
     *
     * @param sender sender that has to be notified about an update
     * @return true if the sender has been notified
     */
    public boolean notifyPlayer(CommandSender sender) {
        boolean needUpdate = this.hasToBeUpdated();

        if (needUpdate) {
            sender.sendMessage(PREFIX + "§eA new version is available for download! §7Follow instructions in your console to update the plugin.");
        }

        return needUpdate;
    }

    /**
     * Detects the latest version of the plugin and stores it.
     * Also notifies the console about the check.
     */
    @Override
    public void run() {
        try {
            this.retreiveVersions();
            this.notifyConsole();
        } catch (IOException e) {
            this.plugin.getLogger().log(Level.SEVERE, "Cannot retrieve the latest version of the plugin", e);
        }
    }

    /**
     * Notify the console if the plugin have to be updated or not.
     */
    private void notifyConsole() {
        Logger logger = this.plugin.getLogger();

        if (this.hasToBeUpdated()) {
            logger.log(Level.WARNING, "**** PLUGIN UPDATE {0}", this.latestVersion);
            logger.log(Level.WARNING, "Your server is using the version {0} of EnderContainers.", this.currentVersion);
            logger.warning(" ");
            logger.log(Level.WARNING, "  1. Download the new version here: {0}", DOWNLOAD_LINK);
            logger.log(Level.WARNING, "  2. Then follow the upgrade guide {0} here: {1}", new String[]{this.latestVersion, WIKI_LINK});
            logger.warning(" ");
        } else if (this.isUsingDevelopmentBuild()) {
            logger.warning("**** IMPORTANT!");
            logger.warning("You are using a development build. This means that the plugin can be unstable.");
            logger.warning("If you have an issue during its execution, please report it on the Github repository.");
        } else {
            logger.log(Level.INFO, "You are using the newest version of the plugin ({0}).", this.currentVersion);
        }
    }

    /**
     * Start the checking of the plugin's version.
     *
     * @throws IOException throwed if the method cannot read the remote json
     */
    private void retreiveVersions() throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(new URL(VERSION_URL).openStream()));
        VersionInfo[] infos = new Gson().fromJson(reader, VersionInfo[].class);

        if (infos.length > 0) {
            this.currentVersion = this.plugin.getDescription().getVersion();
            this.latestVersion = infos[0].name;
        } else {
            throw new IOException("Malformated remote version json");
        }

        this.currentVersionValue = this.getVersionDecimalValue(this.currentVersion);
        this.latestVersionValue = this.getVersionDecimalValue(this.latestVersion);
    }

    /**
     * Parse a version string to get its decimal representation.
     *
     * @param version version to parse
     * @return the decimal value of the version
     */
    private long getVersionDecimalValue(String version) {
        int decimalValue = Integer.parseInt(version.replaceAll("[^\\d]", "")) * 100;

        // Manage sub-version character at the end of the version
        char subVersion = version.charAt(version.length() - 1);
        if (subVersion >= 'a' && subVersion <= 'z') {
            decimalValue += (subVersion - 'a') + 1;
        }

        return decimalValue;
    }

    /**
     * Compares decimal values of current and latest versions to check
     * if the plugin needs to be updated.
     *
     * @return true if the plugin needs to be updated
     */
    private boolean hasToBeUpdated() {
        return this.currentVersionValue < this.latestVersionValue;
    }

    /**
     * Compares decimal values of current and latest versions to check
     * if the server runs a development build of the plugin.
     *
     * @return true if the plugin is a development build
     */
    private boolean isUsingDevelopmentBuild() {
        return this.currentVersionValue > this.latestVersionValue;
    }

    /**
     * Version info retreived from the Spiget API
     */
    private static class VersionInfo {
        String name;
    }

}

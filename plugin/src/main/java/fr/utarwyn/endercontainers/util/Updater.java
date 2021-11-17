package fr.utarwyn.endercontainers.util;

import com.google.gson.Gson;
import fr.utarwyn.endercontainers.AbstractManager;
import fr.utarwyn.endercontainers.compatibility.CompatibilityHelper;
import fr.utarwyn.endercontainers.configuration.Files;
import fr.utarwyn.endercontainers.configuration.LocaleKey;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Collections;
import java.util.logging.Level;

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
     * Current version of the plugin
     */
    private SemanticVersion currentVersion;

    /**
     * Latest version of the plugin
     */
    private SemanticVersion latestVersion;

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize() {
        this.registerListener(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void load() {
        // Check for updates if enabled by the server administrator
        if (Files.getConfiguration().isUpdateChecker()) {
            this.plugin.getServer().getScheduler().runTaskAsynchronously(this.plugin, this);
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
    }

    /**
     * Detects the latest version of the plugin and stores it.
     * Also notifies the console about the check.
     */
    @Override
    public void run() {
        try {
            this.retreiveVersions();
            this.plugin.executeTaskOnMainThread(this::notifyConsole);
        } catch (IOException e) {
            this.plugin.getLogger().log(Level.SEVERE,
                    "Cannot retrieve the latest version of the plugin", e);
        }
    }

    /**
     * Method called when a player joins the server
     *
     * @param event The join event
     */
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // Send update message to the player if it has the permission
        if (player.hasPermission("endercontainers.update") && this.hasToBeUpdated()) {
            this.notifyPlayer(player);
        }
    }

    /**
     * Notify the console if the plugin has to be updated or not.
     */
    public void notifyConsole() {
        if (this.hasToBeUpdated()) {
            logger.warning("-----------[Plugin Update]----------");
            logger.log(Level.WARNING, "  Your server is using v{0} of EnderContainers. Latest is v{1}.",
                    new Object[]{this.currentVersion, this.latestVersion});
            logger.warning("");
            logger.log(Level.WARNING, "  Download the new version here: {0}", DOWNLOAD_LINK);

            if (this.currentVersion.getMinor() != this.latestVersion.getMinor() || this.currentVersion.getMajor() != this.latestVersion.getMajor()) {
                logger.log(Level.WARNING, "  Then follow the upgrade guide to v{0} here: {1}",
                        new Object[]{this.latestVersion, WIKI_LINK});
            }

            logger.warning("------------------------------------");
        } else {
            logger.log(Level.INFO, "You are using the newest version of the plugin ({0}).", this.currentVersion);
        }

        if (this.currentVersion.isDevelopment()) {
            logger.warning("----------[Unstable Build]----------");
            logger.warning("  You are using a development build. This means that the plugin can be unstable.");
            logger.warning("  If you have an issue during its execution, please report it on the Github repository.");
            logger.warning("------------------------------------");
        }
    }

    /**
     * Notify player if the plugin has to be updated or not.
     *
     * @param player player that will be notified
     */
    public void notifyPlayer(Player player) {
        if (this.hasToBeUpdated()) {
            PluginMsg.errorMessage(
                    player, LocaleKey.CMD_UPDATE,
                    Collections.singletonMap("version", this.latestVersion.toString())
            );
            Sound sound = CompatibilityHelper.searchSound("NOTE_PLING", "BLOCK_NOTE_PLING", "BLOCK_NOTE_BLOCK_PLING");
            player.playSound(player.getLocation(), sound, 1f, 1f);
        } else {
            PluginMsg.infoMessage(
                    player, LocaleKey.CMD_NO_UPDATE,
                    Collections.singletonMap("version", this.latestVersion.toString())
            );
        }
    }

    /**
     * Start the checking of the plugin's version.
     *
     * @throws IOException throwed if the method cannot read the remote json
     */
    private void retreiveVersions() throws IOException {
        try (InputStream inputStream = new URL(VERSION_URL).openStream();
             InputStreamReader streamReader = new InputStreamReader(inputStream);
             BufferedReader reader = new BufferedReader(streamReader)) {
            VersionInfo[] infos = new Gson().fromJson(reader, VersionInfo[].class);

            if (infos.length > 0) {
                String pluginVersion = this.plugin.getDescription().getVersion();
                this.currentVersion = new SemanticVersion(pluginVersion);
                this.latestVersion = new SemanticVersion(infos[0].name);
            } else {
                throw new IOException("Malformated remote version json");
            }
        }
    }

    /**
     * Compares current and latest versions to check if the plugin has to be updated.
     *
     * @return true if the plugin needs to be updated
     */
    private boolean hasToBeUpdated() {
        return this.currentVersion != null && this.latestVersion != null
                && this.currentVersion.compareTo(this.latestVersion) < 0;
    }

    /**
     * Version info retreived from the Spiget API
     */
    private static class VersionInfo {
        String name;
    }

}

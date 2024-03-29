package fr.utarwyn.endercontainers.util;

import com.google.common.base.Strings;
import fr.utarwyn.endercontainers.configuration.Files;
import fr.utarwyn.endercontainers.configuration.LocaleKey;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class used which sends various messages.
 *
 * @author Utarwyn
 * @since 1.0.0
 */
public class PluginMsg {

    private static final String PREFIX_ERROR = "✖ ";
    private static final String PREFIX_SUCCESS = "✔ ";
    private static final String PREFIX_INFO = "→ ";
    private static final String PREFIX_CONSOLE = "[EnderContainers] ";

    private PluginMsg() {
        // Not implemented
    }

    /**
     * Send a simple error message from
     * a locale key to a specific entity.
     *
     * @param receiver receiver of the message
     * @param key      key of the message to send with in an error state
     */
    public static void errorMessage(CommandSender receiver, LocaleKey key) {
        PluginMsg.errorMessage(receiver, key, null);
    }

    /**
     * Send a simple error message from
     * a locale key to a specific entity.
     *
     * @param receiver   receiver of the message
     * @param key        key of the message to send with in an error state
     * @param parameters parameters to replace in the message
     */
    public static void errorMessage(CommandSender receiver, LocaleKey key,
                                    Map<String, String> parameters) {
        PluginMsg.sendLocalizedMessage(receiver, PREFIX_ERROR, ChatColor.RED, key, parameters);
    }

    /**
     * Send a simple success message from
     * a locale key to a specific entity.
     *
     * @param receiver receiver of the message
     * @param key      key of the message to send with in a success state
     */
    public static void successMessage(CommandSender receiver, LocaleKey key) {
        PluginMsg.successMessage(receiver, key, null);
    }

    /**
     * Send a simple success message from
     * a locale key to a specific entity.
     *
     * @param receiver   receiver of the message
     * @param key        key of the message to send with in a success state
     * @param parameters parameters to replace in the message
     */
    public static void successMessage(CommandSender receiver, LocaleKey key,
                                      Map<String, String> parameters) {
        PluginMsg.sendLocalizedMessage(receiver, PREFIX_SUCCESS, ChatColor.GREEN, key, parameters);
    }

    /**
     * Send a simple informative message from
     * a locale key to a specific entity.
     *
     * @param receiver receiver of the message
     * @param key      key of the message to send with in an info state
     */
    public static void infoMessage(CommandSender receiver, LocaleKey key) {
        PluginMsg.infoMessage(receiver, key, null);
    }

    /**
     * Send a simple informative message from
     * a locale key to a specific entity.
     *
     * @param receiver   receiver of the message
     * @param key        key of the message to send with in an info state
     * @param parameters parameters to replace in the message
     */
    public static void infoMessage(CommandSender receiver, LocaleKey key,
                                   Map<String, String> parameters) {
        PluginMsg.sendLocalizedMessage(receiver, PREFIX_INFO, ChatColor.GRAY, key, parameters);
    }

    /**
     * Send an access denied message to a specific entity.
     *
     * @param receiver receiver of the message
     */
    public static void accessDenied(CommandSender receiver) {
        if (receiver instanceof Player) {
            errorMessage(receiver, LocaleKey.ERR_NOPERM_PLAYER);
        } else {
            errorMessage(receiver, LocaleKey.ERR_NOPERM_CONSOLE);
        }
    }

    /**
     * Send the plugin header bar to a specific entity.
     *
     * @param receiver receiver of the message
     */
    public static void pluginBar(CommandSender receiver) {
        String pBar = "§5§m" + Strings.repeat("-", 5);
        String sBar = "§d§m" + Strings.repeat("-", 11);

        receiver.sendMessage("§8++" + pBar + sBar + "§r§d( §6EnderContainers §d)" + sBar + pBar + "§8++");
    }

    /**
     * Send the plugin footer bar to a specific entity.
     *
     * @param receiver receiver of the message
     */
    public static void endBar(CommandSender receiver) {
        String pBar = "§5§m" + Strings.repeat("-", 5);
        receiver.sendMessage("§8++" + pBar + "§d§m" + Strings.repeat("-", 39) + pBar + "§8++");
    }

    /**
     * Send a localized message with parameters to a specific entity.
     *
     * @param receiver   receiver of the message
     * @param prefix     text to prepend to the message
     * @param color      color of the message
     * @param key        localized key of the message
     * @param parameters parameters to replace in the message
     */
    private static void sendLocalizedMessage(CommandSender receiver, String prefix, ChatColor color,
                                             LocaleKey key, Map<String, String> parameters) {
        StringBuilder sb = new StringBuilder();

        // Add a specific prefix when displaying messages in console
        if (receiver instanceof ConsoleCommandSender) {
            sb.append(PREFIX_CONSOLE);
            sb.append(color);
        } else {
            sb.append(color);
            sb.append(prefix);
        }
        sb.append(Files.getLocale().getMessage(key));

        if (parameters != null && !parameters.isEmpty()) {
            parameters.forEach((k, v) -> {
                String replacement = v + color;
                Matcher m = Pattern.compile("%" + k + "%").matcher(sb);
                int start = 0;

                while (m.find(start)) {
                    sb.replace(m.start(), m.end(), replacement);
                    start = m.start() + replacement.length();
                }
            });
        }

        receiver.sendMessage(sb.toString());
    }

}

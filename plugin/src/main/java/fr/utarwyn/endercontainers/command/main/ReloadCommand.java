package fr.utarwyn.endercontainers.command.main;

import fr.utarwyn.endercontainers.EnderContainers;
import fr.utarwyn.endercontainers.Managers;
import fr.utarwyn.endercontainers.command.AbstractCommand;
import fr.utarwyn.endercontainers.configuration.Files;
import fr.utarwyn.endercontainers.configuration.LocaleKey;
import fr.utarwyn.endercontainers.configuration.wrapper.YamlFileLoadException;
import fr.utarwyn.endercontainers.util.PluginMsg;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ReloadCommand extends AbstractCommand {

    public ReloadCommand() {
        super("reload", "rl");

        this.setPermission("reload");
    }

    @Override
    public void perform(CommandSender sender) {
        String advice = sender instanceof Player
                ? "See the console for more info!"
                : "See above error log.";

        try {
            // Reload all configuration files
            Files.getConfiguration().load();
            Files.getLocale().load();

            // Reload managers and inform the sender
            Managers.reloadAll();
            PluginMsg.messageWithPrefix(sender, LocaleKey.CMD_CONFIG_RELOADED, ChatColor.GREEN);
        } catch (YamlFileLoadException e) {
            sender.sendMessage(EnderContainers.PREFIX +
                    "§cError when reloading a configuration file! " + advice);
            sender.sendMessage(EnderContainers.PREFIX + "§8Cannot fully reload the plugin.");
        }
    }

}

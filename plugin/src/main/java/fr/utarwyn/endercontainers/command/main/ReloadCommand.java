package fr.utarwyn.endercontainers.command.main;

import fr.utarwyn.endercontainers.Managers;
import fr.utarwyn.endercontainers.command.AbstractCommand;
import fr.utarwyn.endercontainers.configuration.ConfigLoadingException;
import fr.utarwyn.endercontainers.configuration.Files;
import fr.utarwyn.endercontainers.configuration.LocaleKey;
import fr.utarwyn.endercontainers.util.PluginMsg;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

import java.util.logging.Level;

public class ReloadCommand extends AbstractCommand {

    private final Plugin plugin;

    public ReloadCommand(Plugin plugin) {
        super("reload", "rl");
        this.plugin = plugin;

        this.setPermission("endercontainers.reload");
    }

    @Override
    public void perform(CommandSender sender) {
        try {
            // Reload all configuration files
            Files.reload(this.plugin);

            // Reload managers and inform the sender
            Managers.reloadAll();
            PluginMsg.successMessage(sender, LocaleKey.CMD_CONFIG_RELOADED);
        } catch (ConfigLoadingException e) {
            PluginMsg.errorMessage(sender, LocaleKey.ERR_RELOAD_ERROR);
            this.plugin.getLogger().log(Level.SEVERE,
                    "Cannot load plugin configuration or messages file", e);
        }
    }

}

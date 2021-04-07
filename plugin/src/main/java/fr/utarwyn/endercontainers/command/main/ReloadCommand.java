package fr.utarwyn.endercontainers.command.main;

import fr.utarwyn.endercontainers.EnderContainers;
import fr.utarwyn.endercontainers.Managers;
import fr.utarwyn.endercontainers.command.AbstractCommand;
import fr.utarwyn.endercontainers.configuration.Files;
import fr.utarwyn.endercontainers.configuration.LocaleKey;
import fr.utarwyn.endercontainers.configuration.wrapper.YamlFileLoadException;
import fr.utarwyn.endercontainers.util.PluginMsg;
import org.bukkit.command.CommandSender;

import java.util.logging.Level;

public class ReloadCommand extends AbstractCommand {

    public ReloadCommand() {
        super("reload", "rl");

        this.setPermission("reload");
    }

    @Override
    public void perform(CommandSender sender) {
        try {
            // Reload all configuration files
            Files.getConfiguration().load();
            Files.getLocale().load();

            // Reload managers and inform the sender
            Managers.reloadAll();
            PluginMsg.successMessage(sender, LocaleKey.CMD_CONFIG_RELOADED);
        } catch (YamlFileLoadException e) {
            PluginMsg.errorMessage(sender, LocaleKey.ERR_RELOAD_ERROR);
            EnderContainers.getInstance().getLogger().log(Level.SEVERE,
                    "Cannot load plugin configuration or messages file", e);
        }
    }

}

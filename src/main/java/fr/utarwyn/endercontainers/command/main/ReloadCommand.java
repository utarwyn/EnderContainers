package fr.utarwyn.endercontainers.command.main;

import fr.utarwyn.endercontainers.EnderContainers;
import fr.utarwyn.endercontainers.Managers;
import fr.utarwyn.endercontainers.command.AbstractCommand;
import fr.utarwyn.endercontainers.configuration.Files;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class ReloadCommand extends AbstractCommand {

    public ReloadCommand() {
        super("reload", "rl");

        this.setPermission("reload");
    }

    @Override
    public void perform(CommandSender sender) {
        if (!Files.getConfiguration().reload()) {
            sender.sendMessage(EnderContainers.PREFIX + "§cError when reloading config! See the console for more info!");
            sender.sendMessage(EnderContainers.PREFIX + "§8Plugin now disabled.");

            Bukkit.getPluginManager().disablePlugin(EnderContainers.getInstance());
            return;
        }

        if (!Files.getLocale().reload()) {
            sender.sendMessage(EnderContainers.PREFIX + "§cError when reloading plugin locale! See the console for more info!");
            sender.sendMessage(EnderContainers.PREFIX + "§8Plugin now disabled.");

            Bukkit.getPluginManager().disablePlugin(EnderContainers.getInstance());
            return;
        }

        Managers.reloadAll();

        this.sendTo(sender, ChatColor.GREEN + Files.getLocale().getConfigReloaded());
    }

}

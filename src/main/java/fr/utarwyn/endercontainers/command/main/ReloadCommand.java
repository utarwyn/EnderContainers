package fr.utarwyn.endercontainers.command.main;

import fr.utarwyn.endercontainers.EnderContainers;
import fr.utarwyn.endercontainers.Managers;
import fr.utarwyn.endercontainers.command.AbstractCommand;
import fr.utarwyn.endercontainers.configuration.Files;
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
        String advice = sender instanceof Player ? "See the console for more info!" : "See above error log.";

        if (!Files.getConfiguration().reload()) {
            sender.sendMessage(EnderContainers.PREFIX + "§cError when reloading config! " + advice);
            sender.sendMessage(EnderContainers.PREFIX + "§8Cannot fully reload the plugin.");
            return;
        }

        if (!Files.getLocale().reload()) {
            sender.sendMessage(EnderContainers.PREFIX + "§cError when reloading plugin locale! " + advice);
            sender.sendMessage(EnderContainers.PREFIX + "§8Cannot fully reload the plugin.");
            return;
        }

        Managers.reloadAll();

        this.sendTo(sender, ChatColor.GREEN + Files.getLocale().getConfigReloaded());
    }

}

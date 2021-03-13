package fr.utarwyn.endercontainers.command;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.*;

public abstract class CommandTestHelper<T extends AbstractCommand> {

    protected T command;

    protected String permission;

    protected void run(CommandSender sender, String... args) {
        this.command.execute(sender, null, args);
    }

    protected List<String> tabComplete(CommandSender sender, String... args) {
        return this.command.onTabComplete(sender, this.command, this.command.getName(), args);
    }

    protected void givePermission(Player player) {
        this.setPermissionState(player, true);
    }

    protected void setPermissionState(Player player, boolean state) {
        if (this.permission != null) {
            lenient().when(player.hasPermission(this.permission)).thenReturn(state);
        }
    }

    protected void verifyNoPerm(CommandSender sender) {
        verify(sender).sendMessage(contains("perm"));
    }

    protected void verifyNoPerm(CommandSender sender, int count) {
        verify(sender, times(count)).sendMessage(contains("perm"));
    }

}

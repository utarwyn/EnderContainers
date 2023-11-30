package fr.utarwyn.endercontainers.util;

import fr.utarwyn.endercontainers.TestHelper;
import fr.utarwyn.endercontainers.TestInitializationException;
import fr.utarwyn.endercontainers.configuration.LocaleKey;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class PluginMsgTest {

    @Mock
    private Player player;

    @Mock
    private ConsoleCommandSender console;

    @BeforeAll
    public static void setUpClass() throws TestInitializationException {
        TestHelper.setUpFiles();
    }

    @Test
    public void errorMessage() {
        PluginMsg.errorMessage(this.player, LocaleKey.CMD_NO_UPDATE);
        verify(this.player).sendMessage(startsWith("§c"));
    }

    @Test
    public void successMessage() {
        PluginMsg.successMessage(this.player, LocaleKey.CMD_NO_UPDATE);
        verify(this.player).sendMessage(startsWith("§a"));
    }

    @Test
    public void infoMessage() {
        PluginMsg.infoMessage(this.player, LocaleKey.CMD_NO_UPDATE);
        verify(this.player).sendMessage(startsWith("§7"));
    }

    @Test
    public void accessDenied() {
        PluginMsg.accessDenied(this.player);
        verify(this.player).sendMessage(contains("don't have the permission"));
        PluginMsg.accessDenied(this.console);
        verify(this.console).sendMessage(contains("must be a player"));
    }

    @Test
    public void pluginBar() {
        PluginMsg.pluginBar(this.console);
        verify(this.console).sendMessage(contains("EnderContainers"));
    }

    @Test
    public void endBar() {
        PluginMsg.endBar(this.console);
        verify(this.console).sendMessage(contains("++"));
    }

}

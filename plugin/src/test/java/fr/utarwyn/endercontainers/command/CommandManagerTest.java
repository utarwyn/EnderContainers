package fr.utarwyn.endercontainers.command;

import fr.utarwyn.endercontainers.TestHelper;
import fr.utarwyn.endercontainers.TestInitializationException;
import fr.utarwyn.endercontainers.mock.v1_15.ServerMock;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandMap;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.SimpleCommandMap;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class CommandManagerTest {

    private static final String NAME = "name";
    private static final String ALIAS = "alias";

    private CommandManager manager;

    @BeforeClass
    public static void setUpClass() {
        TestHelper.setUpServer();
    }

    private static void registerCommandMap(CommandMap commandMap) {
        when(((ServerMock) Bukkit.getServer()).getCommandMap()).thenReturn(commandMap);
    }

    @Before
    public void setUp() throws TestInitializationException {
        this.manager = new CommandManager();
        TestHelper.setupManager(this.manager);
    }

    @Test
    public void registerCommand() throws ReflectiveOperationException {
        CommandMap commandMap = mock(CommandMap.class);
        registerCommandMap(commandMap);

        AbstractCommand command = mock(AbstractCommand.class);
        this.manager.register(command);
        verify(commandMap).register("endercontainers", command);
    }

    @Test
    public void unregisterCommand() {
        CommandMap commandMap = new SimpleCommandMap(Bukkit.getServer());
        registerCommandMap(commandMap);

        PluginCommand command = mock(PluginCommand.class);
        when(command.getName()).thenReturn(NAME);
        when(command.toString()).thenReturn(NAME);
        when(command.getAliases()).thenReturn(Collections.singletonList(ALIAS));
        commandMap.register("endercontainers", command);

        assertThat(commandMap.getCommand(NAME)).isNotNull().isEqualTo(command);
        assertThat(commandMap.getCommand(ALIAS)).isNotNull().isEqualTo(command);

        this.manager.unregister(command);

        assertThat(commandMap.getCommand(NAME)).isNull();
        assertThat(commandMap.getCommand(ALIAS)).isNull();
    }

    @Test
    public void cannotUnregisterIfPluginCommand() {
        CommandMap commandMap = new SimpleCommandMap(Bukkit.getServer());
        registerCommandMap(commandMap);

        PluginCommand command = mock(PluginCommand.class);
        when(command.getName()).thenReturn(NAME);
        when(command.getAliases()).thenReturn(Collections.singletonList(ALIAS));

        AbstractCommand pluginCommand = mock(AbstractCommand.class);
        when(pluginCommand.getName()).thenReturn(NAME);
        when(pluginCommand.toString()).thenReturn(NAME);
        when(pluginCommand.getAliases()).thenReturn(Collections.singletonList(ALIAS));
        commandMap.register("endercontainers", pluginCommand);

        this.manager.unregister(command);

        assertThat(commandMap.getCommand(NAME)).isNotNull().isEqualTo(pluginCommand);
        assertThat(commandMap.getCommand(ALIAS)).isNotNull().isEqualTo(pluginCommand);
    }

}

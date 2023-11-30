package fr.utarwyn.endercontainers;

import org.bukkit.Server;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.logging.Logger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AbstractManagerTest {

    @Mock(answer = Answers.CALLS_REAL_METHODS)
    private AbstractManager manager;

    @Mock
    private EnderContainers plugin;

    @Test
    public void registerListener() {
        Server server = mock(Server.class);
        PluginManager pluginManager = mock(PluginManager.class);

        when(this.plugin.getServer()).thenReturn(server);
        when(server.getPluginManager()).thenReturn(pluginManager);

        Listener listener = mock(Listener.class);

        this.manager.plugin = this.plugin;
        this.manager.registerListener(listener);

        verify(pluginManager).registerEvents(listener, this.plugin);
    }

    @Test
    public void setPlugin() {
        Logger logger = mock(Logger.class);
        when(this.plugin.getLogger()).thenReturn(logger);

        this.manager.setPlugin(this.plugin);

        assertThat(this.manager.plugin).isNotNull().isEqualTo(plugin);
        assertThat(this.manager.logger).isNotNull().isEqualTo(logger);
    }

}

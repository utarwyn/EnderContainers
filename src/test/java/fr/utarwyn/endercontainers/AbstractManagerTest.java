package fr.utarwyn.endercontainers;

import org.bukkit.Server;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.logging.Logger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class AbstractManagerTest {

    private EnderContainers plugin;

    private AbstractManager manager;

    @Before
    public void setUp() {
        this.manager = mock(AbstractManager.class, Mockito.CALLS_REAL_METHODS);
        this.plugin = mock(EnderContainers.class);
    }

    @Test
    public void registerListener() {
        Server server = mock(Server.class);
        PluginManager pluginManager = mock(PluginManager.class);

        when(this.plugin.getServer()).thenReturn(server);
        when(server.getPluginManager()).thenReturn(pluginManager);

        Listener listener = mock(Listener.class);

        this.manager.plugin = this.plugin;
        this.manager.registerListener(listener);

        verify(pluginManager, times(1)).registerEvents(listener, this.plugin);
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

package fr.utarwyn.endercontainers;

import fr.utarwyn.endercontainers.command.CommandManager;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class EnderContainersTest {

    private EnderContainers plugin;

    @BeforeClass
    public static void setUpClass() throws TestInitializationException {
        TestHelper.setUpFiles();
        Managers.instances.clear();
    }

    @Before
    public void setUp() throws TestInitializationException {
        this.plugin = TestHelper.getPlugin();
        doCallRealMethod().when(this.plugin).onEnable();
        doCallRealMethod().when(this.plugin).onDisable();
        doCallRealMethod().when(this.plugin).executeTaskOnMainThread(any());
        doCallRealMethod().when(this.plugin).executeTaskOnOtherThread(any());
    }

    @Test
    public void enable() {
        Map instances = mock(Map.class);
        CommandManager commandManager = mock(CommandManager.class);

        when(instances.containsKey(any())).thenReturn(true);
        when(instances.get(CommandManager.class)).thenReturn(commandManager);

        System.setProperty("bstats.relocatecheck", "false");
        Managers.instances = instances;

        this.plugin.onEnable();
        verify(instances, times(9)).containsKey(any());
        verify(commandManager).registerCommands();

        Managers.instances = new LinkedHashMap<>();
    }

    @Test
    public void disable() {
        this.plugin.onDisable();
        assertThat(Managers.instances).isEmpty();
    }

    @Test
    public void executeTaskOnMainThread() {
        Runnable run = mock(Runnable.class);

        // Primary thread
        when(this.plugin.getServer().isPrimaryThread()).thenReturn(true);
        this.plugin.executeTaskOnMainThread(run);
        verify(run).run();

        // Asynchronous thread
        when(this.plugin.getServer().isPrimaryThread()).thenReturn(false);
        this.plugin.executeTaskOnMainThread(run);
        verify(this.plugin.getServer().getScheduler()).scheduleSyncDelayedTask(this.plugin, run);
        verify(run, times(2)).run();
    }

    @Test
    public void executeTaskOnOtherThread() {
        Runnable run = mock(Runnable.class);
        this.plugin.executeTaskOnOtherThread(run);
        verify(this.plugin.getServer().getScheduler()).runTaskAsynchronously(this.plugin, run);
        verify(run).run();
    }

}

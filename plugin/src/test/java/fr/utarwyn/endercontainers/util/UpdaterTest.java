package fr.utarwyn.endercontainers.util;

import fr.utarwyn.endercontainers.EnderContainers;
import fr.utarwyn.endercontainers.TestHelper;
import fr.utarwyn.endercontainers.configuration.wrapper.YamlFileLoadException;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * All tests about {@link Updater} class.
 *
 * @author Utarwyn
 * @since 2.2.1
 */
@RunWith(MockitoJUnitRunner.class)
public class UpdaterTest {

    private Updater updater;

    private EnderContainers plugin;

    @Mock
    private CommandSender sender;

    @BeforeClass
    public static void setUpClass() throws IOException, YamlFileLoadException,
            InvalidConfigurationException, ReflectiveOperationException {
        TestHelper.setUpFiles();
    }

    @Before
    public void setUp() throws ReflectiveOperationException {
        this.updater = new Updater();
        this.plugin = TestHelper.getPlugin();
        TestHelper.setupManager(this.updater);
    }

    @Test
    public void withUpdate() {
        when(this.plugin.getDescription().getVersion()).thenReturn("1.0.0");
        this.updater.load();

        verify(this.plugin.getServer().getScheduler()).runTaskAsynchronously(this.plugin, this.updater);
        assertThat(this.updater.notifyPlayer(this.sender)).isTrue();
    }

    @Test
    public void withoutUpdate() {
        when(this.plugin.getDescription().getVersion()).thenReturn("99.99.99");
        this.updater.load();

        verify(this.plugin.getServer().getScheduler()).runTaskAsynchronously(this.plugin, this.updater);
        assertThat(this.updater.notifyPlayer(this.sender)).isFalse();
    }

    @Test
    public void unload() {
        this.updater.unload();
        assertThat(this.updater.notifyPlayer(this.sender)).isFalse();
    }

}

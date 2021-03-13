package fr.utarwyn.endercontainers.command.main;

import fr.utarwyn.endercontainers.TestHelper;
import fr.utarwyn.endercontainers.command.CommandTestHelper;
import fr.utarwyn.endercontainers.configuration.wrapper.YamlFileLoadException;
import fr.utarwyn.endercontainers.util.Updater;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.Player;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class UpdateCommandTest extends CommandTestHelper<UpdateCommand> {

    @Mock
    private Player player;

    @Mock
    private Updater updater;

    @BeforeClass
    public static void setUpClass() throws ReflectiveOperationException, YamlFileLoadException,
            InvalidConfigurationException, IOException {
        TestHelper.setUpFiles();
    }

    @Before
    public void setUp() throws ReflectiveOperationException {
        TestHelper.registerManagers(this.updater);
        this.command = new UpdateCommand();
        this.permission = "endercontainers.update";
    }

    @Test
    public void create() {
        assertThat(this.command.getName()).isEqualTo("update");
    }

    @Test
    public void foundUpdate() {
        this.givePermission(this.player);
        when(this.updater.notifyPlayer(this.player)).thenReturn(true);

        this.run(this.player);
        verify(this.player, never()).sendMessage(anyString());
    }

    @Test
    public void noUpdate() {
        this.givePermission(this.player);
        when(this.updater.notifyPlayer(this.player)).thenReturn(false);

        this.run(this.player);
        verify(this.player).sendMessage(contains("no update"));
    }

    @Test
    public void noPermission() {
        this.run(this.player);
        this.verifyNoPerm(this.player);
    }

}

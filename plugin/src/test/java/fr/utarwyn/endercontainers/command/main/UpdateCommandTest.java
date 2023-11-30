package fr.utarwyn.endercontainers.command.main;

import fr.utarwyn.endercontainers.TestHelper;
import fr.utarwyn.endercontainers.TestInitializationException;
import fr.utarwyn.endercontainers.command.CommandTestHelper;
import fr.utarwyn.endercontainers.util.Updater;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class UpdateCommandTest extends CommandTestHelper<UpdateCommand> {

    @Mock
    private Player player;

    @Mock
    private Updater updater;

    @BeforeAll
    public static void setUpClass() throws TestInitializationException {
        TestHelper.setUpFiles();
    }

    @BeforeEach
    public void setUp() throws TestInitializationException {
        TestHelper.registerManagers(this.updater);
        this.command = new UpdateCommand();
        this.permission = "endercontainers.update";
    }

    @Test
    public void create() {
        assertThat(this.command.getName()).isEqualTo("update");
    }

    @Test
    public void sentByPlayer() {
        this.givePermission(this.player);
        this.run(this.player);
        verify(this.updater).notifyPlayer(this.player);
    }

    @Test
    public void sentByConsole() {
        this.run(mock(ConsoleCommandSender.class));
        verify(this.updater).notifyConsole();
    }

    @Test
    public void noPermission() {
        this.run(this.player);
        this.verifyNoPerm(this.player);
    }

}

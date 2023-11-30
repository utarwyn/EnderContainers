package fr.utarwyn.endercontainers.command.main;

import fr.utarwyn.endercontainers.TestHelper;
import fr.utarwyn.endercontainers.TestInitializationException;
import fr.utarwyn.endercontainers.command.CommandTestHelper;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class ReloadCommandTest extends CommandTestHelper<ReloadCommand> {

    @Mock
    private Player player;

    @BeforeAll
    public static void setUpClass() throws TestInitializationException {
        TestHelper.registerManagers();
        TestHelper.setUpFiles();
    }

    @BeforeEach
    public void setUp() throws TestInitializationException {
        this.command = new ReloadCommand(TestHelper.getPlugin());
        this.permission = "endercontainers.reload";
    }

    @Test
    public void create() {
        assertThat(this.command.getName()).isEqualTo("reload");
        assertThat(this.command.getAliases()).containsExactly("rl");
    }

    @Test
    public void successful() {
        this.givePermission(this.player);
        this.run(this.player);
        verify(this.player).sendMessage(contains("reloaded"));
    }

    @Test
    public void noPermission() {
        this.run(this.player);
        this.verifyNoPerm(this.player);
    }

}

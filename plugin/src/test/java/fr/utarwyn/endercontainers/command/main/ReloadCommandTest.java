package fr.utarwyn.endercontainers.command.main;

import fr.utarwyn.endercontainers.TestHelper;
import fr.utarwyn.endercontainers.TestInitializationException;
import fr.utarwyn.endercontainers.command.CommandTestHelper;
import org.bukkit.entity.Player;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class ReloadCommandTest extends CommandTestHelper<ReloadCommand> {

    @Mock
    private Player player;

    @BeforeClass
    public static void setUpClass() throws TestInitializationException {
        TestHelper.registerManagers();
        TestHelper.setUpFiles();
    }

    @Before
    public void setUp() {
        this.command = new ReloadCommand();
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

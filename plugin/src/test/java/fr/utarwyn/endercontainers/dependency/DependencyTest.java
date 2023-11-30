package fr.utarwyn.endercontainers.dependency;

import fr.utarwyn.endercontainers.mock.DependencyMock;
import org.bukkit.plugin.Plugin;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class DependencyTest {

    @Test
    public void create() {
        Plugin plugin = mock(Plugin.class);
        Dependency dependency = new DependencyMock(plugin);

        // Check the registered plugin
        assertThat(dependency.getPlugin()).isEqualTo(plugin);

        // By default these methods do nothing
        dependency.onEnable();
        dependency.onDisable();
    }

}

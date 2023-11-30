package fr.utarwyn.endercontainers.dependency.resolve;

import fr.utarwyn.endercontainers.dependency.Dependency;
import fr.utarwyn.endercontainers.mock.DependencyMock;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DependencyResolverTest {

    @Mock
    private PluginManager pluginManager;

    @Mock
    private Plugin plugin;

    @BeforeEach
    public void setUp() {
        // Create a fake description file for the plugin
        PluginDescriptionFile descriptionFile = mock(PluginDescriptionFile.class);
        lenient().when(descriptionFile.getVersion()).thenReturn("1.12.5");
        lenient().when(descriptionFile.getAuthors()).thenReturn(Collections.singletonList("Utarwyn"));

        // Plugin manager stubs
        when(this.plugin.getDescription()).thenReturn(descriptionFile);
        when(this.pluginManager.isPluginEnabled(anyString())).thenReturn(false);
        when(this.pluginManager.isPluginEnabled("Plugin")).thenReturn(true);
        when(this.pluginManager.getPlugin("Plugin")).thenReturn(this.plugin);
    }

    @Test
    public void resolveFailure() {
        DependencyResolver resolver = new DependencyResolver(this.pluginManager);

        // Empty name
        assertThatNullPointerException().isThrownBy(resolver::resolve)
                .withNoCause().withMessageContaining("name");

        // Empty patterns
        resolver.name("NoPlugin");
        assertThatNullPointerException().isThrownBy(resolver::resolve)
                .withNoCause().withMessageContaining("matcher");

        // Disabled plugin
        resolver.use(Dependency.class);
        assertThat(resolver.resolve()).isEmpty();

        // Wrong dependency class with enabled plugin
        resolver.name("Plugin");
        assertThatIllegalStateException().isThrownBy(resolver::resolve)
                .withCauseInstanceOf(ReflectiveOperationException.class)
                .withMessageContaining("cannot instanciate");
    }

    @Test
    public void resolveWithUse() {
        DependencyResolver resolver = new DependencyResolver(this.pluginManager)
                .name("Plugin").use(DependencyMock.class);

        Optional<Dependency> dependency = resolver.resolve();

        assertThat(dependency).isNotEmpty();
        assertThat(dependency.get().getPlugin())
                .isNotNull().isEqualTo(this.plugin);

        Dependency dep = dependency.get();
        assertThatExceptionOfType(UnsupportedOperationException.class)
                .isThrownBy(() -> dep.validateBlockChestOpening(null, null))
                .withNoCause().withMessage(null);
    }

    @Test
    public void resolveWithMatcher() {
        DependencyResolver resolver = new DependencyResolver(this.pluginManager)
                .name("Plugin").matchVersion("^2.*", Dependency.class);

        // Unknown version
        assertThat(resolver.resolve()).isEmpty();

        // Matched version
        resolver.matchVersion("^1.*", DependencyMock.class);
        assertThat(resolver.resolve()).isNotEmpty();
    }

    @Test
    public void resolvingOrder() {
        DependencyResolver resolver = new DependencyResolver(this.pluginManager)
                .name("Plugin")
                .matchAuthor("Utarwyn", DependencyMock.class)
                .use(null);

        assertThat(resolver.resolve()).isNotEmpty();
    }

}

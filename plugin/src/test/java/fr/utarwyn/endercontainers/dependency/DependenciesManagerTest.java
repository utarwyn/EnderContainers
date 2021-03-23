package fr.utarwyn.endercontainers.dependency;

import fr.utarwyn.endercontainers.TestHelper;
import fr.utarwyn.endercontainers.TestInitializationException;
import fr.utarwyn.endercontainers.configuration.LocaleKey;
import fr.utarwyn.endercontainers.dependency.exceptions.BlockChestOpeningException;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class DependenciesManagerTest {

    private DependenciesManager manager;

    @BeforeClass
    public static void setUpClass() {
        TestHelper.setUpServer();
    }

    @Before
    public void setUp() throws TestInitializationException {
        this.manager = new DependenciesManager();
        TestHelper.setupManager(this.manager);
    }

    @Test
    public void loadWithoutDependency() {
        this.manager.load();
        assertThat(this.manager.getDependencies()).isNotNull().isEmpty();
    }

    @Test
    public void loadWithDependencies() {
        this.registerFakePlugin("WorldGuard", "7.6");
        this.manager.load();

        // Check the loaded dependency
        assertThat(this.manager.getDependencies())
                .isNotNull().isNotEmpty().hasSize(1);

        Dependency dependency = this.manager.getDependencies().iterator().next();
        assertThat(dependency.getPlugin()).isNotNull();
    }

    @Test
    public void unload() {
        Dependency dependency = mock(Dependency.class);

        this.registerFakeDependencies(dependency);
        this.manager.unload();

        verify(dependency, times(1)).onDisable();
        assertThat(this.manager.getDependencies()).isNotNull().isEmpty();
    }

    @Test
    public void validateBlockChestOpening() throws BlockChestOpeningException {
        Dependency dep1 = mock(Dependency.class);
        Dependency dep2 = mock(Dependency.class);
        Block block = mock(Block.class);
        Player player = mock(Player.class);

        this.registerFakeDependencies(dep1, dep2);
        this.manager.validateBlockChestOpening(block, player);

        verify(dep1, times(1)).validateBlockChestOpening(block, player);
        verify(dep2, times(1)).validateBlockChestOpening(block, player);
    }

    @Test
    public void blockChestOpeningException() {
        Map<String, String> parameters = Collections.singletonMap("faction", "test");

        BlockChestOpeningException exception1 = new BlockChestOpeningException();
        BlockChestOpeningException exception2 = new BlockChestOpeningException(
                LocaleKey.ERR_DEP_FACTIONS);
        BlockChestOpeningException exception3 = new BlockChestOpeningException(
                LocaleKey.ERR_DEP_FACTIONS, parameters);

        assertThat(exception1.getKey()).isNull();
        assertThat(exception1.getParameters()).isNull();
        assertThat(exception2.getKey()).isNotNull().isEqualTo(LocaleKey.ERR_DEP_FACTIONS);
        assertThat(exception2.getParameters()).isNull();
        assertThat(exception3.getKey()).isNotNull().isEqualTo(LocaleKey.ERR_DEP_FACTIONS);
        assertThat(exception3.getParameters()).isNotNull().isEqualTo(parameters);
    }

    /**
     * Register a fake plugin to the Bukkit manager.
     *
     * @param name    name of the fake plugin
     * @param version version of the fake plugin
     */
    private void registerFakePlugin(String name, String version) {
        PluginManager manager = Bukkit.getServer().getPluginManager();
        Plugin plugin = mock(Plugin.class);
        PluginDescriptionFile descriptionFile = mock(PluginDescriptionFile.class);

        when(plugin.getName()).thenReturn(name);
        when(plugin.getDescription()).thenReturn(descriptionFile);
        when(descriptionFile.getVersion()).thenReturn(version);
        when(manager.isPluginEnabled(name)).thenReturn(true);
        when(manager.getPlugin(name)).thenReturn(plugin);
    }

    /**
     * Register fake dependencies in the manager.
     *
     * @param dependencies list of dependencies to register
     */
    private void registerFakeDependencies(Dependency... dependencies) {
        this.manager.getDependencies().clear();
        this.manager.getDependencies().addAll(Arrays.asList(dependencies));
    }

}

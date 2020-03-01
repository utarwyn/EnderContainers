package fr.utarwyn.endercontainers;

import fr.utarwyn.endercontainers.configuration.Files;
import fr.utarwyn.endercontainers.configuration.Locale;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.PluginManager;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;

import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.logging.Logger;

import static org.mockito.Mockito.*;

/**
 * Helper class for testing purposes.
 *
 * @author Utarwyn
 * @since 2.2.0
 */
public class TestHelper {

    private static boolean serverReady = false;

    private static boolean filesReady = false;

    private TestHelper() {

    }

    /**
     * Setup a mocked version of the Bukkit server.
     */
    public static synchronized void setUpServer() {
        if (!serverReady) {
            Server server = mock(Server.class);
            Logger logger = Logger.getGlobal();
            BukkitScheduler scheduler = mock(BukkitScheduler.class);
            PluginManager pluginManager = mock(PluginManager.class);

            lenient().when(server.getLogger()).thenReturn(logger);
            lenient().when(server.getScheduler()).thenReturn(scheduler);
            lenient().when(server.getPluginManager()).thenReturn(pluginManager);

            TestHelper.mockSchedulers(server);
            Bukkit.setServer(server);

            serverReady = true;
        }
    }

    /**
     * Setup a mocked version of configuration files.
     */
    public static synchronized void setUpFiles() throws IOException,
            InvalidConfigurationException, ReflectiveOperationException {
        if (!filesReady) {
            // Initialize the configuration object
            EnderContainers plugin = mock(EnderContainers.class);
            FileConfiguration config = new YamlConfiguration();

            config.load(new InputStreamReader(TestHelper.class.getResourceAsStream("/config.yml")));
            when(plugin.getConfig()).thenReturn(config);

            Files.initConfiguration(plugin);

            // Initialize the locale object
            Field localeField = Files.class.getDeclaredField("locale");
            Locale locale = mock(Locale.class, RETURNS_SMART_NULLS);

            localeField.setAccessible(true);
            localeField.set(null, locale);
            localeField.setAccessible(false);

            filesReady = true;
        }
    }

    /**
     * Setup a mocked version of an abstract manager.
     *
     * @param manager manager to mock
     * @throws ReflectiveOperationException thrown if cannot setup the manager correctly
     */
    public static void setupManager(AbstractManager manager) throws ReflectiveOperationException {
        TestHelper.setUpServer();

        EnderContainers plugin = mock(EnderContainers.class);
        Field field = manager.getClass().getSuperclass().getDeclaredField("plugin");

        when(plugin.getServer()).thenReturn(Bukkit.getServer());

        field.setAccessible(true);
        field.set(manager, plugin);
        field.setAccessible(false);
    }

    /**
     * Register fake managers.
     *
     * @param managers managers to register
     * @throws ReflectiveOperationException thrown if cannot register managers
     */
    @SuppressWarnings("unchecked")
    public static void registerManagers(AbstractManager... managers) throws ReflectiveOperationException {
        Field field = Managers.class.getDeclaredField("instances");

        field.setAccessible(true);

        Map<Class<?>, AbstractManager> instances = (Map<Class<?>, AbstractManager>) field.get(null);
        instances.clear();

        for (AbstractManager manager : managers) {
            instances.put(manager.getClass(), manager);
        }

        field.setAccessible(false);
    }

    /**
     * Mock basic server scheduler methods to perform
     * all task in the same thread as testing scripts.
     *
     * @param server mocked server
     */
    private static void mockSchedulers(Server server) {
        lenient().when(server.getScheduler().scheduleSyncDelayedTask(
                any(), any(Runnable.class)
        )).then(answer -> {
            answer.getArgument(1, Runnable.class).run();
            return 1;
        });

        lenient().when(server.getScheduler().runTaskAsynchronously(
                any(), any(Runnable.class)
        )).then(answer -> {
            answer.getArgument(1, Runnable.class).run();
            return mock(BukkitTask.class);
        });
    }

}

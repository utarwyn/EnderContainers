package fr.utarwyn.endercontainers;

import fr.utarwyn.endercontainers.configuration.Files;
import fr.utarwyn.endercontainers.configuration.wrapper.YamlFileLoadException;
import fr.utarwyn.endercontainers.mock.ItemFactoryMock;
import fr.utarwyn.endercontainers.mock.ItemMetaMock;
import fr.utarwyn.endercontainers.mock.v1_12.ServerMock;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.UnsafeValues;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.UUID;
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

    private static EnderContainers plugin = null;

    private static Player player = null;

    private TestHelper() {

    }

    /**
     * Setup a mocked version of the Bukkit server.
     */
    public static synchronized void setUpServer() {
        if (!serverReady) {
            Server server = mock(ServerMock.class);
            Logger logger = Logger.getGlobal();
            BukkitScheduler scheduler = mock(BukkitScheduler.class);
            PluginManager pluginManager = mock(PluginManager.class);

            lenient().when(server.getLogger()).thenReturn(logger);
            lenient().when(server.getScheduler()).thenReturn(scheduler);
            lenient().when(server.getPluginManager()).thenReturn(pluginManager);

            TestHelper.mockSchedulers(server);
            TestHelper.mockInventoryObjects(server);
            Bukkit.setServer(server);

            serverReady = true;
        }
    }

    /**
     * Setup a mocked version of configuration files.
     */
    public static synchronized void setUpFiles() throws IOException, YamlFileLoadException,
            ReflectiveOperationException, InvalidConfigurationException {
        if (!filesReady) {
            // Initialize the configuration object
            EnderContainers plugin = TestHelper.getPlugin();
            FileConfiguration config = new YamlConfiguration();
            String localePath = TestHelper.class.getResource("/locale.yml").getPath();

            config.load(new InputStreamReader(TestHelper.class.getResourceAsStream("/config.test.yml")));

            when(plugin.getConfig()).thenReturn(config);
            when(plugin.getDataFolder()).thenReturn(new File(localePath).getParentFile());

            Files.initConfiguration(plugin);
            Files.initLocale(plugin);

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

        EnderContainers plugin = TestHelper.getPlugin();
        Field pluginField = manager.getClass().getSuperclass().getDeclaredField("plugin");
        Field loggerField = manager.getClass().getSuperclass().getDeclaredField("logger");

        pluginField.setAccessible(true);
        pluginField.set(manager, plugin);
        pluginField.setAccessible(false);

        loggerField.setAccessible(true);
        loggerField.set(manager, plugin.getLogger());
        loggerField.setAccessible(false);
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
     * Retrieve a mocked instance of the plugin.
     *
     * @return mocked instance of the plugin
     */
    public static EnderContainers getPlugin() throws ReflectiveOperationException {
        if (plugin == null) {
            TestHelper.setUpServer();
            Server server = Bukkit.getServer();

            plugin = mock(EnderContainers.class);

            Field staticAccess = EnderContainers.class.getDeclaredField("instance");
            staticAccess.setAccessible(true);
            staticAccess.set(null, plugin);
            staticAccess.setAccessible(false);

            lenient().when(plugin.getServer()).thenReturn(server);
            lenient().when(plugin.getDescription()).thenReturn(mock(PluginDescriptionFile.class));
            lenient().doReturn(server.getLogger()).when(plugin).getLogger();
            lenient().doAnswer(answer -> {
                answer.getArgument(0, Runnable.class).run();
                return null;
            }).when(plugin).executeTaskOnMainThread(any());
        }

        return plugin;
    }

    /**
     * Retrieve a mocked instance of a fake connected player.
     *
     * @return mocked instance of a player
     */
    public static Player getPlayer() {
        if (player == null) {
            TestHelper.setUpServer();

            UUID uuid = UUID.randomUUID();
            player = mock(Player.class);

            when(player.isOnline()).thenReturn(true);
            when(player.getUniqueId()).thenReturn(uuid);
            when(player.getName()).thenReturn("Utarwyn");

            when(Bukkit.getServer().getPlayer(uuid)).thenReturn(player);
        }

        return player;
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

    /**
     * Mock objects and methods related to inventories and itemstacks.
     *
     * @param server mocked server
     */
    private static void mockInventoryObjects(Server server) {
        // Inventory creation
        lenient().when(server.createInventory(
                any(InventoryHolder.class), anyInt(), anyString()
        )).thenAnswer(answer -> {
            Inventory inventory = mock(Inventory.class);
            lenient().when(inventory.getContents()).thenReturn(new ItemStack[0]);
            return inventory;
        });

        // Register mocked item meta class in the serialization object
        ConfigurationSerialization.registerClass(ItemMetaMock.class);

        // Unsafe values and item factory
        UnsafeValues unsafeValues = mock(UnsafeValues.class);

        lenient().when(unsafeValues.getDataVersion()).thenReturn(1);
        lenient().when(unsafeValues.getMaterial(anyString(), anyInt()))
                .then(answer -> Material.valueOf(answer.getArgument(0, String.class)));
        lenient().when(server.getUnsafe()).thenReturn(unsafeValues);
        lenient().when(server.getItemFactory()).thenReturn(new ItemFactoryMock());
    }

}

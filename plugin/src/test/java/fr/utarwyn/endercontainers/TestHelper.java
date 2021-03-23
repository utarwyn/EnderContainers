package fr.utarwyn.endercontainers;

import fr.utarwyn.endercontainers.compatibility.nms.NMSHologramUtil;
import fr.utarwyn.endercontainers.configuration.Configuration;
import fr.utarwyn.endercontainers.configuration.Files;
import fr.utarwyn.endercontainers.configuration.wrapper.YamlFileLoadException;
import fr.utarwyn.endercontainers.mock.ItemFactoryMock;
import fr.utarwyn.endercontainers.mock.ItemMetaMock;
import fr.utarwyn.endercontainers.mock.v1_15.ServerMock;
import org.bukkit.*;
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
import java.util.Collections;
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

    private static UUID playerIdentifier = null;

    private TestHelper() {

    }

    /**
     * Setup a mocked version of the Bukkit server.
     */
    public static synchronized void setUpServer() {
        if (!serverReady) {
            Server server = mock(ServerMock.class);

            lenient().when(server.getLogger()).thenReturn(Logger.getGlobal());
            lenient().when(server.getScheduler()).thenReturn(mock(BukkitScheduler.class));
            lenient().when(server.getPluginManager()).thenReturn(mock(PluginManager.class));
            lenient().when(server.getOfflinePlayer(anyString())).thenReturn(mock(OfflinePlayer.class));

            TestHelper.mockSchedulers(server);
            TestHelper.mockInventoryObjects(server);
            Bukkit.setServer(server);

            serverReady = true;
        }
    }

    /**
     * Setup a mocked version of configuration files.
     *
     * @throws TestInitializationException thrown if cannot setup files properly
     */
    public static synchronized void setUpFiles() throws TestInitializationException {
        if (!filesReady) {
            try {
                // Initialize the configuration object
                EnderContainers plugin = TestHelper.getPlugin();
                FileConfiguration config = new YamlConfiguration();
                String localePath = TestHelper.class.getResource("/locale.yml").getPath();

                config.load(new InputStreamReader(TestHelper.class.getResourceAsStream("/config.test.yml")));

                when(plugin.getConfig()).thenReturn(config);
                when(plugin.getDataFolder()).thenReturn(new File(localePath).getParentFile());

                Files.initConfiguration(plugin);
                Files.initLocale(plugin);
            } catch (IOException | YamlFileLoadException | InvalidConfigurationException e) {
                throw new TestInitializationException(e);
            }

            filesReady = true;
        }
    }

    /**
     * Setup a mocked version of an abstract manager.
     *
     * @param manager manager to mock
     * @throws TestInitializationException thrown if cannot setup the manager correctly
     */
    public static void setupManager(AbstractManager manager) throws TestInitializationException {
        try {
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
        } catch (ReflectiveOperationException e) {
            throw new TestInitializationException(e);
        }
    }

    /**
     * Register fake managers.
     *
     * @param managers managers to register
     * @throws TestInitializationException thrown if cannot register managers
     */
    @SuppressWarnings("unchecked")
    public static void registerManagers(AbstractManager... managers) throws TestInitializationException {
        try {
            Field field = Managers.class.getDeclaredField("instances");

            field.setAccessible(true);

            Map<Class<?>, AbstractManager> instances = (Map<Class<?>, AbstractManager>) field.get(null);
            instances.clear();

            for (AbstractManager manager : managers) {
                instances.put(manager.getClass(), manager);
            }

            field.setAccessible(false);
        } catch (ReflectiveOperationException e) {
            throw new TestInitializationException(e);
        }
    }

    /**
     * Retrieve a mocked instance of the plugin.
     *
     * @return mocked instance of the plugin
     */
    public static EnderContainers getPlugin() throws TestInitializationException {
        if (plugin == null) {
            TestHelper.setUpServer();
            Server server = Bukkit.getServer();

            plugin = mock(EnderContainers.class);

            try {
                Field staticAccess = EnderContainers.class.getDeclaredField("instance");
                staticAccess.setAccessible(true);
                staticAccess.set(null, plugin);
                staticAccess.setAccessible(false);
            } catch (ReflectiveOperationException e) {
                throw new TestInitializationException(e);
            }

            lenient().when(plugin.getServer()).thenReturn(server);
            lenient().when(plugin.getDescription()).thenReturn(mock(PluginDescriptionFile.class));
            lenient().doReturn(server.getLogger()).when(plugin).getLogger();
            lenient().doAnswer(answer -> {
                answer.getArgument(0, Runnable.class).run();
                return null;
            }).when(plugin).executeTaskOnMainThread(any());

            // Also setup NMS classes
            try {
                Field hologramUtilStaticAccess = NMSHologramUtil.class.getDeclaredField("instance");
                hologramUtilStaticAccess.setAccessible(true);
                hologramUtilStaticAccess.set(null, mock(NMSHologramUtil.class));
                hologramUtilStaticAccess.setAccessible(false);
            } catch (ReflectiveOperationException e) {
                throw new TestInitializationException(e);
            }
        }

        return plugin;
    }

    /**
     * Retrieve a mocked instance of a fake connected player.
     *
     * @return mocked instance of a player
     */
    public static Player getPlayer() {
        if (playerIdentifier == null) {
            TestHelper.setUpServer();
            playerIdentifier = UUID.randomUUID();
        }

        World world = mock(World.class);
        Player player = mock(Player.class);
        Inventory enderChest = mock(Inventory.class);

        lenient().when(world.getName()).thenReturn("world");
        lenient().when(player.getWorld()).thenReturn(world);
        lenient().when(player.isOnline()).thenReturn(true);
        lenient().when(player.getUniqueId()).thenReturn(playerIdentifier);
        lenient().when(player.getName()).thenReturn("Utarwyn");
        lenient().when(player.getServer()).thenReturn(Bukkit.getServer());
        lenient().when(player.canSee(any())).thenReturn(true);
        lenient().when(enderChest.getContents()).thenReturn(new ItemStack[0]);
        lenient().when(player.getEnderChest()).thenReturn(enderChest);

        lenient().when(Bukkit.getServer().getPlayer(playerIdentifier)).thenReturn(player);
        lenient().when(Bukkit.getServer().getPlayer("Utarwyn")).thenReturn(player);
        lenient().doReturn(Collections.singletonList(player)).when(Bukkit.getServer()).getOnlinePlayers();

        return player;
    }

    /**
     * Overrides a configuration value in a specific unit test.
     *
     * @param fieldName field to override
     * @param value     value which will replace the current one
     * @throws TestInitializationException thrown if cannot override configuration key
     */
    public static void overrideConfigurationValue(String fieldName, Object value)
            throws TestInitializationException {
        setUpFiles();

        try {
            Field field = Configuration.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(Files.getConfiguration(), value);
            field.setAccessible(false);
        } catch (ReflectiveOperationException e) {
            throw new TestInitializationException(e);
        }

        filesReady = false;
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

        lenient().when(server.getScheduler().runTaskTimer(
                any(), any(Runnable.class), anyLong(), anyLong()
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
    @SuppressWarnings("deprecation")
    private static void mockInventoryObjects(Server server) {
        // Inventory creation
        lenient().when(server.createInventory(
                any(InventoryHolder.class), anyInt(), anyString()
        )).thenAnswer(answer -> {
            Inventory inventory = mock(Inventory.class);
            lenient().when(inventory.getContents()).thenReturn(new ItemStack[0]);
            lenient().when(inventory.getHolder()).thenReturn(answer.getArgument(0, InventoryHolder.class));
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

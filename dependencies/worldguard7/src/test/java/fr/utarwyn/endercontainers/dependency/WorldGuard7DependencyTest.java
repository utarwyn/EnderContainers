package fr.utarwyn.endercontainers.dependency;

import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.internal.platform.WorldGuardPlatform;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import fr.utarwyn.endercontainers.dependency.exceptions.BlockChestOpeningException;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class WorldGuard7DependencyTest {

    private static boolean serverReplaced = false;

    private WorldGuard7Dependency dependency;

    @Mock
    private RegionQuery regionQuery;

    @Mock
    private LocalPlayer localPlayer;

    @Mock
    private Player player;

    @Mock
    private Block block;

    @BeforeAll
    public static void setUpClass() throws ReflectiveOperationException {
        if (Bukkit.getServer() == null) {
            Server server = mock(Server.class);
            when(server.getVersion()).thenReturn("(MC: 1.16.5)");

            Field serverField = Bukkit.class.getDeclaredField("server");
            serverField.setAccessible(true);
            serverField.set(null, server);
            serverField.setAccessible(false);

            serverReplaced = true;
        }

        Field instanceField = WorldEditPlugin.class.getDeclaredField("INSTANCE");
        instanceField.setAccessible(true);
        instanceField.set(null, mock(WorldEditPlugin.class));
        instanceField.setAccessible(false);
    }

    @AfterAll
    public static void tearDownClass() throws ReflectiveOperationException {
        if (serverReplaced) {
            Field instance = Bukkit.class.getDeclaredField("server");
            instance.setAccessible(true);
            instance.set(null, null);
            instance.setAccessible(false);
        }
    }

    @BeforeEach
    public void setUp() {
        WorldGuardPlugin plugin = mock(WorldGuardPlugin.class);
        WorldGuardPlatform platform = mock(WorldGuardPlatform.class);
        RegionContainer regionContainer = mock(RegionContainer.class);

        this.dependency = new WorldGuard7Dependency(plugin);

        lenient().when(regionContainer.createQuery()).thenReturn(this.regionQuery);
        lenient().when(platform.getRegionContainer()).thenReturn(regionContainer);
        lenient().when(plugin.wrapPlayer(this.player)).thenReturn(this.localPlayer);
        lenient().when(this.block.getLocation()).thenReturn(new Location(mock(World.class), 0, 0, 0));

        WorldGuard.getInstance().setPlatform(platform);
    }

    @Test
    public void validateIfOperator() {
        try {
            when(this.player.isOp()).thenReturn(true);
            this.dependency.validateBlockChestOpening(this.block, this.player);
        } catch (BlockChestOpeningException e) {
            fail("should not throw block chest opening exception because player is an operator");
        }
    }

    @Test
    public void validateIfCanBuildSucceed() {
        try {
            when(this.regionQuery.testBuild(any(com.sk89q.worldedit.util.Location.class), eq(this.localPlayer), eq(Flags.CHEST_ACCESS)))
                    .thenReturn(true);
            this.dependency.validateBlockChestOpening(this.block, this.player);
        } catch (BlockChestOpeningException e) {
            fail("should not throw block chest opening exception because build is authorized by WorldGuard");
        }
    }

    @Test
    public void invalidateIfCanBuildErrored() {
        try {
            when(this.regionQuery.testBuild(any(com.sk89q.worldedit.util.Location.class), eq(this.localPlayer), eq(Flags.CHEST_ACCESS)))
                    .thenReturn(false);
            this.dependency.validateBlockChestOpening(this.block, this.player);
            fail("should throw block chest opening exception because build is forbidden by WorldGuard");
        } catch (BlockChestOpeningException ignored) {
        }
    }

}

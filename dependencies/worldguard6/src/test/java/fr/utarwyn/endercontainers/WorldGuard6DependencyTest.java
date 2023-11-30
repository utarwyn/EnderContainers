package fr.utarwyn.endercontainers;

import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.bukkit.RegionContainer;
import com.sk89q.worldguard.bukkit.RegionQuery;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import fr.utarwyn.endercontainers.dependency.WorldGuard6Dependency;
import fr.utarwyn.endercontainers.dependency.exceptions.BlockChestOpeningException;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class WorldGuard6DependencyTest {

    private WorldGuard6Dependency dependency;

    @Mock
    private RegionQuery regionQuery;

    @Mock
    private Player player;

    @Mock
    private Block block;

    @BeforeEach
    public void setUp() {
        WorldGuardPlugin plugin = mock(WorldGuardPlugin.class, CALLS_REAL_METHODS);
        RegionContainer regionContainer = mock(RegionContainer.class);

        this.dependency = new WorldGuard6Dependency(plugin);

        lenient().when(regionContainer.createQuery()).thenReturn(this.regionQuery);
        lenient().when(plugin.getRegionContainer()).thenReturn(regionContainer);
        lenient().when(this.block.getLocation()).thenReturn(new Location(mock(World.class), 0, 0, 0));
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
            when(this.regionQuery.testBuild(eq(this.block.getLocation()), any(LocalPlayer.class), eq(DefaultFlag.CHEST_ACCESS)))
                    .thenReturn(true);
            this.dependency.validateBlockChestOpening(this.block, this.player);
        } catch (BlockChestOpeningException e) {
            fail("should not throw block chest opening exception because build is authorized by WorldGuard");
        }
    }

    @Test
    public void invalidateIfCanBuildErrored() {
        try {
            when(this.regionQuery.testBuild(eq(this.block.getLocation()), any(LocalPlayer.class), eq(DefaultFlag.CHEST_ACCESS)))
                    .thenReturn(false);
            this.dependency.validateBlockChestOpening(this.block, this.player);
            fail("should throw block chest opening exception because build is forbidden by WorldGuard");
        } catch (BlockChestOpeningException ignored) {
        }
    }

}

package fr.utarwyn.endercontainers.dependency;

import com.plotsquared.core.PlotSquared;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.plot.PlotArea;
import com.plotsquared.core.plot.flag.implementations.UseFlag;
import com.plotsquared.core.plot.flag.types.BlockTypeWrapper;
import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.block.BlockTypes;
import fr.utarwyn.endercontainers.configuration.LocaleKey;
import fr.utarwyn.endercontainers.dependency.exceptions.BlockChestOpeningException;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PlotSquaredDependencyTest {

    private PlotSquaredDependency dependency;

    @Mock
    private PlotArea plotArea;

    @Mock
    private Plot plot;

    @Mock
    private Player player;

    @Mock
    private Block block;

    @BeforeClass
    public static void setUpClass() {
        BlockType.REGISTRY.register("minecraft:ender_chest", new BlockType("ender_chest"));
    }

    @Before
    public void setUp() throws ReflectiveOperationException {
        this.dependency = new PlotSquaredDependency(null);

        PlotSquared plotSquared = mock(PlotSquared.class);
        Field plotInstance = PlotSquared.class.getDeclaredField("instance");
        plotInstance.setAccessible(true);
        plotInstance.set(null, plotSquared);
        plotInstance.setAccessible(false);

        when(this.player.getUniqueId()).thenReturn(UUID.randomUUID());
        when(this.block.getLocation()).thenReturn(new Location(mock(World.class), 0, 0, 0));
        when(plotSquared.getPlotAreaAbs(any())).thenReturn(this.plotArea);
        when(this.plotArea.getPlot(any(com.plotsquared.core.location.Location.class))).thenReturn(this.plot);
        when(this.plot.getFlag(UseFlag.class)).thenReturn(Collections.singletonList(BlockTypeWrapper.get(BlockTypes.ENDER_CHEST)));
    }

    @Test
    public void invalidateIfPlotProtected() {
        try {
            this.dependency.validateBlockChestOpening(this.block, this.player);
            fail("should throw block chest opening exception when not in plot");
        } catch (BlockChestOpeningException e) {
            assertThat(e.getKey()).isEqualTo(LocaleKey.ERR_DEP_PLOTSQ);
            assertThat(e.getParameters()).isNull();
        }
    }

    @Test
    public void validateNotInPlot() {
        try {
            when(this.plotArea.getPlot(any(com.plotsquared.core.location.Location.class))).thenReturn(null);
            this.dependency.validateBlockChestOpening(this.block, this.player);
        } catch (BlockChestOpeningException e) {
            fail("should not throw block chest opening exception when not in plot");
        }
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
    public void validateIfPermGranted() {
        try {
            when(this.plot.isAdded(this.player.getUniqueId())).thenReturn(true);
            this.dependency.validateBlockChestOpening(this.block, this.player);
        } catch (BlockChestOpeningException e) {
            fail("should not throw block chest opening exception because player has permission");
        }
    }

    @Test
    public void validateIfNoProtection() {
        try {
            when(this.plot.getFlag(UseFlag.class)).thenReturn(new ArrayList<>());
            this.dependency.validateBlockChestOpening(this.block, this.player);
        } catch (BlockChestOpeningException e) {
            fail("should not throw block chest opening exception because player has permission");
        }
    }

}

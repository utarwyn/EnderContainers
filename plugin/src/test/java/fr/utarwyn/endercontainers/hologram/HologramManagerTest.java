package fr.utarwyn.endercontainers.hologram;

import fr.utarwyn.endercontainers.TestHelper;
import fr.utarwyn.endercontainers.TestInitializationException;
import fr.utarwyn.endercontainers.dependency.DependenciesManager;
import fr.utarwyn.endercontainers.dependency.exceptions.BlockChestOpeningException;
import fr.utarwyn.endercontainers.enderchest.EnderChestManager;
import fr.utarwyn.endercontainers.enderchest.context.PlayerContext;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class HologramManagerTest {

    private HologramManager manager;

    private Player observer;

    @Mock
    private Block targetedBlock;

    @Mock
    private PlayerContext context;

    @Mock
    private DependenciesManager dependenciesManager;

    @Mock
    private EnderChestManager enderChestManager;

    @BeforeClass
    public static void setUpClass() throws TestInitializationException {
        TestHelper.setUpServer();
        TestHelper.setUpFiles();
    }

    @Before
    @SuppressWarnings("unchecked")
    public void setUp() throws TestInitializationException {
        this.observer = TestHelper.getPlayer();
        this.manager = new HologramManager();

        TestHelper.registerManagers(this.dependenciesManager, this.enderChestManager);
        TestHelper.setupManager(this.manager);

        when(this.targetedBlock.getType()).thenReturn(Material.ENDER_CHEST);
        when(this.targetedBlock.getLocation()).thenReturn(new Location(mock(World.class), 0, 0, 0));
        when(this.observer.getTargetBlock(isNull(), anyInt())).thenReturn(this.targetedBlock);
        doAnswer(answer -> {
            ((Consumer<PlayerContext>) answer.getArgument(1)).accept(this.context);
            return null;
        }).when(this.enderChestManager).loadPlayerContext(any(), any());
    }

    @After
    public void tearDown() {
        lenient().when(this.observer.getWorld().getName()).thenReturn("world");
        lenient().when(this.observer.getTargetBlock(isNull(), anyInt())).thenReturn(mock(Block.class));
    }

    @Test
    public void taskTimer() throws TestInitializationException {
        this.manager.load();
        verify(Bukkit.getServer().getScheduler()).runTaskTimer(
                eq(TestHelper.getPlugin()), eq(this.manager), anyLong(), anyLong()
        );
    }

    @Test
    public void unload() {
        this.manager.load();
        this.manager.unload();
        assertThat(this.manager.task).isNull();
        assertThat(this.manager.holograms).isEmpty();
    }

    @Test
    public void spawnHologram() throws BlockChestOpeningException {
        assertThat(this.manager.holograms).isNull();

        // Spawn hologram when targeting enderchest
        this.manager.load();

        verify(this.dependenciesManager).validateBlockChestOpening(this.targetedBlock, this.observer);
        verify(this.enderChestManager).loadPlayerContext(eq(this.observer.getUniqueId()), any(Consumer.class));
        assertThat(this.manager.holograms).isNotEmpty().hasSize(1);
        assertThat(this.manager.loadingPlayers).isEmpty();
    }

    @Test
    public void disabledByDependency() throws BlockChestOpeningException {
        doThrow(new BlockChestOpeningException()).when(this.dependenciesManager)
                .validateBlockChestOpening(this.targetedBlock, this.observer);

        this.manager.load();
        assertThat(this.manager.holograms).isEmpty();
    }

    @Test
    public void disabledWorld() {
        when(this.observer.getWorld().getName()).thenReturn("disabled");
        this.manager.load();
        assertThat(this.manager.holograms).isEmpty();
    }

    @Test
    public void disabledIfContextLoading() {
        assertThat(this.manager.loadingPlayers).isNull();
        this.manager.load();

        this.manager.holograms.clear();
        this.manager.loadingPlayers.add(this.observer.getUniqueId());
        this.manager.run();

        assertThat(this.manager.holograms).isEmpty();
    }

    @Test
    public void dispawnHologram() {
        // Spawn hologram after first load
        this.manager.load();
        assertThat(this.manager.holograms).isNotEmpty().hasSize(1);

        // Dispawn hologram when targeting AIR right after
        when(this.targetedBlock.getType()).thenReturn(Material.AIR);
        this.manager.run();
        assertThat(this.manager.holograms).isEmpty();
    }

}

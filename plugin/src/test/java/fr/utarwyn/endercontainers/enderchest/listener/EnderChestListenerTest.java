package fr.utarwyn.endercontainers.enderchest.listener;

import fr.utarwyn.endercontainers.TestHelper;
import fr.utarwyn.endercontainers.TestInitializationException;
import fr.utarwyn.endercontainers.configuration.LocaleKey;
import fr.utarwyn.endercontainers.dependency.DependenciesManager;
import fr.utarwyn.endercontainers.dependency.exceptions.BlockChestOpeningException;
import fr.utarwyn.endercontainers.enderchest.EnderChestManager;
import fr.utarwyn.endercontainers.enderchest.context.PlayerContext;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.world.WorldSaveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.bukkit.event.block.Action.LEFT_CLICK_AIR;
import static org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
public class EnderChestListenerTest {

    private EnderChestListener listener;

    @Mock
    private EnderChestManager manager;

    @Mock
    private DependenciesManager dependenciesManager;

    @Mock
    private Player player;

    @Mock
    private Block block;

    @Mock
    private World world;

    @BeforeEach
    public void setUp() throws TestInitializationException {
        TestHelper.registerManagers(this.manager, this.dependenciesManager);
        TestHelper.setUpFiles();

        this.listener = new EnderChestListener(this.manager);

        lenient().when(this.player.getWorld()).thenReturn(this.world);
        lenient().when(this.world.getName()).thenReturn("world");
        lenient().when(this.block.getType()).thenReturn(Material.ENDER_CHEST);
        lenient().when(this.player.getUniqueId()).thenReturn(UUID.randomUUID());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void playerInteract() {
        ArgumentCaptor<Consumer<PlayerContext>> consumer = ArgumentCaptor.forClass(Consumer.class);

        // Normal case, player should got access to its enderchest
        PlayerInteractEvent event = this.createInteractEvent(RIGHT_CLICK_BLOCK);
        this.listener.onPlayerInteract(event);

        verify(this.manager).loadPlayerContext(eq(this.player.getUniqueId()), consumer.capture());
        assertThat(event.useInteractedBlock()).isEqualTo(Event.Result.DENY);
        assertThat(event.useItemInHand()).isEqualTo(Event.Result.DENY);

        // Check context consumer after interaction
        PlayerContext context = mock(PlayerContext.class);
        consumer.getValue().accept(context);
        verify(context).openListInventory(this.player, this.block);
    }

    @Test
    public void playerInteractBlockedByDependency() throws BlockChestOpeningException {
        doThrow(new BlockChestOpeningException(LocaleKey.ERR_DEP_FACTIONS, new HashMap<String, String>() {{
            put("faction", "awesome_faction");
        }})).when(this.dependenciesManager).validateBlockChestOpening(this.block, this.player);

        PlayerInteractEvent event = this.createInteractEvent(RIGHT_CLICK_BLOCK);
        this.listener.onPlayerInteract(event);

        assertThat(event.useInteractedBlock()).isEqualTo(Event.Result.DENY);
        assertThat(event.useItemInHand()).isEqualTo(Event.Result.DENY);

        verify(this.player).sendMessage(contains("can't use the enderchest in awesome_faction"));
    }

    @Test
    public void playerInteractNoBlockAction() {
        // No block registered in this test case
        this.block = null;

        // Bad action?
        PlayerInteractEvent badAction = this.createInteractEvent(LEFT_CLICK_AIR);
        this.listener.onPlayerInteract(badAction);
        assertThat(badAction.useInteractedBlock()).isEqualTo(Event.Result.DENY); // block is null, result is deny

        // No block?
        PlayerInteractEvent noBlock = this.createInteractEvent(RIGHT_CLICK_BLOCK);
        this.listener.onPlayerInteract(noBlock);
        assertThat(noBlock.useInteractedBlock()).isEqualTo(Event.Result.DENY); // block is null, result is deny
    }

    @Test
    public void playerInteractWrongBlockType() {
        when(this.block.getType()).thenReturn(Material.CHEST);
        PlayerInteractEvent event = this.createInteractEvent(RIGHT_CLICK_BLOCK);
        this.listener.onPlayerInteract(event);
        assertThat(event.useInteractedBlock()).isEqualTo(Event.Result.ALLOW);
    }

    @Test
    public void playerInteractWhileSneaking() {
        ItemStack item = new ItemStack(Material.STICK);
        PlayerInventory inventory = mock(PlayerInventory.class);

        when(this.player.getInventory()).thenReturn(inventory);
        when(inventory.getItemInHand()).thenReturn(item);
        when(this.player.isSneaking()).thenReturn(true);

        PlayerInteractEvent event = this.createInteractEvent(RIGHT_CLICK_BLOCK);
        this.listener.onPlayerInteract(event);
        assertThat(event.useInteractedBlock()).isEqualTo(Event.Result.ALLOW);
    }

    @Test
    public void playerInteractDisabledWorld() {
        when(this.world.getName()).thenReturn("disabled");

        PlayerInteractEvent event = this.createInteractEvent(RIGHT_CLICK_BLOCK);
        this.listener.onPlayerInteract(event);
        assertThat(event.useInteractedBlock()).isEqualTo(Event.Result.ALLOW);
    }

    @Test
    public void worldSaveSaveContext() {
        WorldSaveEvent event = new WorldSaveEvent(this.player.getWorld());

        // With a loaded player, we have to save the context
        PlayerContext context = mock(PlayerContext.class);
        Map<UUID, PlayerContext> contextMap = new HashMap<>(Collections.singletonMap(
                this.player.getUniqueId(), context
        ));
        when(context.getOwnerAsObject()).thenReturn(this.player);
        when(this.manager.getContextMap()).thenReturn(contextMap);
        when(this.manager.isContextUnused(this.player.getUniqueId())).thenReturn(true);

        this.listener.onWorldSave(event);
        verify(this.manager).getContextMap();
        verify(this.manager).isContextUnused(this.player.getUniqueId());
        verify(this.manager).savePlayerContext(this.player.getUniqueId(), false);
        assertThat(contextMap).isEmpty();
    }

    @Test
    public void playerLeaveSaveContext() {
        PlayerQuitEvent event = new PlayerQuitEvent(this.player, "");

        // By default, we have to save the context but not delete it
        this.listener.onPlayerQuit(event);
        verify(this.manager).savePlayerContext(this.player.getUniqueId(), false);

        // With an unused context, we also have to delete the context
        when(this.manager.isContextUnused(this.player.getUniqueId())).thenReturn(true);
        this.listener.onPlayerQuit(event);
        verify(this.manager).savePlayerContext(this.player.getUniqueId(), true);
    }

    private PlayerInteractEvent createInteractEvent(Action action) {
        return new PlayerInteractEvent(this.player, action, null, this.block, BlockFace.NORTH);
    }

}

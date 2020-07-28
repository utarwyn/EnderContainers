package fr.utarwyn.endercontainers.enderchest;

import fr.utarwyn.endercontainers.TestHelper;
import fr.utarwyn.endercontainers.configuration.LocaleKey;
import fr.utarwyn.endercontainers.configuration.wrapper.YamlFileLoadException;
import fr.utarwyn.endercontainers.dependency.DependenciesManager;
import fr.utarwyn.endercontainers.dependency.exceptions.BlockChestOpeningException;
import fr.utarwyn.endercontainers.enderchest.context.PlayerContext;
import fr.utarwyn.endercontainers.util.Updater;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;
import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.bukkit.event.block.Action.LEFT_CLICK_AIR;
import static org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
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

    @Before
    public void setUp() throws ReflectiveOperationException, YamlFileLoadException,
            InvalidConfigurationException, IOException {
        TestHelper.registerManagers(this.manager, this.dependenciesManager);
        TestHelper.setUpFiles();

        this.listener = new EnderChestListener(this.manager);

        when(this.player.getWorld()).thenReturn(this.world);
        when(this.player.getLocation()).thenReturn(new Location(this.world, 0, 0, 0));
        when(this.world.getName()).thenReturn("world");
        when(this.block.getType()).thenReturn(Material.ENDER_CHEST);
        when(this.player.getUniqueId()).thenReturn(UUID.randomUUID());
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
        verify(context).openHubMenuFor(eq(this.player), eq(this.block));
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

        verify(this.player).sendMessage(eq("§c§l(!) §cYou can't use the enderchest in awesome_faction§c's territory!"));
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
    public void playerJoinUpdateNotification() throws ReflectiveOperationException {
        PlayerJoinEvent event = new PlayerJoinEvent(this.player, "");

        // Register a fake updater
        Updater updater = mock(Updater.class);
        TestHelper.registerManagers(updater);

        // no permission
        this.listener.onPlayerJoin(event);
        verify(this.player, never()).playSound(any(), any(Sound.class), eq(1f), eq(1f));

        // no update
        when(this.player.isOp()).thenReturn(true);
        when(updater.notifyPlayer(this.player)).thenReturn(false);
        this.listener.onPlayerJoin(event);
        verify(this.player, never()).playSound(any(), any(Sound.class), eq(1f), eq(1f));

        // update and permission
        when(updater.notifyPlayer(this.player)).thenReturn(true);
        this.listener.onPlayerJoin(event);
        verify(this.player).playSound(any(), eq(Sound.BLOCK_NOTE_BLOCK_PLING), eq(1f), eq(1f));
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

    @Test
    public void inventoryCloseSaveOfflineVanillaChest() {
        Player player2 = mock(Player.class);
        UUID player2Identifier = UUID.randomUUID();
        VanillaEnderChest chest = mock(VanillaEnderChest.class);
        InventoryCloseEvent event = this.createInventoryCloseEvent();

        when(player2.getUniqueId()).thenReturn(player2Identifier);
        when(this.manager.getVanillaEnderchestUsedBy(this.player)).thenReturn(Optional.of(chest));

        // do not save if the viewer is the owner of the chest
        when(chest.getOwnerAsPlayer()).thenReturn(this.player);
        this.listener.onInventoryClose(event);
        verify(this.manager, never()).savePlayerContext(player2.getUniqueId(), true);

        // do not save if the player is online
        when(chest.getOwnerAsPlayer()).thenReturn(player2);
        when(chest.getOwner()).thenReturn(player2Identifier);
        when(player2.isOnline()).thenReturn(true);
        this.listener.onInventoryClose(event);
        verify(this.manager, never()).savePlayerContext(player2.getUniqueId(), true);

        // save the chest (and the player data) if the player is not the viewer and its offline
        when(player2.isOnline()).thenReturn(false);
        this.listener.onInventoryClose(event);
        verify(this.manager).savePlayerContext(player2.getUniqueId(), true);
        verify(player2).saveData();
    }

    @Test
    public void inventoryCloseSound() {
        this.listener.onInventoryClose(this.createInventoryCloseEvent());
        verify(this.world).playSound(this.player.getLocation(), Sound.BLOCK_CHEST_CLOSE, 1f, 1f);
    }

    @Test
    public void inventoryCloseUnsupportedActions() {
        InventoryCloseEvent event = this.createInventoryCloseEvent();

        // try with an unknown entity -> no sound
        when(event.getPlayer()).thenReturn(mock(HumanEntity.class));
        this.listener.onInventoryClose(event);
        verify(this.player, never()).playSound(any(), any(Sound.class), anyFloat(), anyFloat());

        // try with another type of container -> no sound
        when(event.getPlayer()).thenReturn(this.player);
        when(event.getInventory().getType()).thenReturn(InventoryType.CHEST);
        this.listener.onInventoryClose(event);
        verify(this.player, never()).playSound(any(), any(Sound.class), anyFloat(), anyFloat());

        // try with an enderchest managed by the plugin -> no sound (integrated in the menu system)
        when(event.getInventory().getType()).thenReturn(InventoryType.ENDER_CHEST);
        when(this.manager.getVanillaEnderchestUsedBy(this.player)).thenReturn(Optional.empty());
        this.listener.onInventoryClose(event);
        verify(this.player, never()).playSound(any(), any(Sound.class), anyFloat(), anyFloat());
    }

    private PlayerInteractEvent createInteractEvent(Action action) {
        return new PlayerInteractEvent(this.player, action, null, this.block, BlockFace.NORTH);
    }

    private InventoryCloseEvent createInventoryCloseEvent() {
        Inventory inventory = mock(Inventory.class);
        InventoryView inventoryView = mock(InventoryView.class);
        InventoryCloseEvent event = new InventoryCloseEvent(inventoryView);

        when(inventory.getType()).thenReturn(InventoryType.ENDER_CHEST);
        when(inventoryView.getPlayer()).thenReturn(this.player);
        when(inventoryView.getTopInventory()).thenReturn(inventory);

        return event;
    }

}

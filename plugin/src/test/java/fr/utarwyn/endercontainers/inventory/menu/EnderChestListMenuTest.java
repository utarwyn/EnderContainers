package fr.utarwyn.endercontainers.inventory.menu;

import fr.utarwyn.endercontainers.TestHelper;
import fr.utarwyn.endercontainers.TestInitializationException;
import fr.utarwyn.endercontainers.enderchest.EnderChest;
import fr.utarwyn.endercontainers.enderchest.context.PlayerContext;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class EnderChestListMenuTest {

    private EnderChestListMenu menu;

    private List<EnderChest> chests;

    @Mock
    private PlayerContext context;

    @BeforeClass
    public static void setUpClass() throws TestInitializationException {
        TestHelper.setUpFiles();
    }

    @Before
    public void setUp() {
        UUID playerId = TestHelper.getPlayer().getUniqueId();

        this.chests = IntStream.range(0, 27).mapToObj((number) -> {
            EnderChest chest = mock(EnderChest.class);
            lenient().when(chest.isAccessible()).thenReturn(true);
            lenient().when(chest.getNum()).thenReturn(number);
            return chest;
        }).collect(Collectors.toList());

        when(this.context.getOwner()).thenReturn(playerId);
        when(this.context.getChest(anyInt())).thenAnswer(
                a -> Optional.ofNullable(this.chests.get(a.getArgument(0, Integer.class)))
        );
        this.menu = new EnderChestListMenu(this.context);
    }

    @Test
    public void prepare() {
        // check that chest has been prepared
        verify(this.chests.get(0)).updateRowCount();

        // check first item
        ItemStack chestItem = this.menu.getInventory().getItem(0);
        assertThat(chestItem).isNotNull();
        assertThat(chestItem.getType()).isEqualTo(Material.LIME_STAINED_GLASS_PANE);

        // check out of bounds item
        assertThat(this.menu.getInventory().getItem(64)).isNull();
    }

    @Test
    public void itemNumbering() {
        Inventory inventory = this.menu.getInventory();
        assertThat(inventory.getItem(1)).isNotNull();
        assertThat(inventory.getItem(1).getAmount()).isEqualTo(2);
        assertThat(inventory.getItem(6)).isNotNull();
        assertThat(inventory.getItem(6).getAmount()).isEqualTo(7);
    }

    @Test
    public void click() {
        Player player = TestHelper.getPlayer();

        // cannot opened the chest by default
        this.menu.onClick(player, 0);
        verify(player).playSound((Location) isNull(), eq(Sound.ENTITY_VILLAGER_NO), anyFloat(), anyFloat());

        // now test if player has perm
        when(this.context.openEnderchestInventory(player, 0)).thenReturn(true);
        this.menu.onClick(player, 0);
        verify(player).playSound((Location) isNull(), eq(Sound.UI_BUTTON_CLICK), anyFloat(), anyFloat());
    }

    @Test
    public void rows() {
        assertThat(this.menu.getRows()).isEqualTo(3);
    }

    @Test
    public void title() {
        String playerName = TestHelper.getPlayer().getName();
        assertThat(this.menu.getTitle()).isEqualTo("Enderchests of " + playerName);
    }

}

package fr.utarwyn.endercontainers.inventory.menu;

import fr.utarwyn.endercontainers.TestHelper;
import fr.utarwyn.endercontainers.TestInitializationException;
import fr.utarwyn.endercontainers.enderchest.EnderChest;
import fr.utarwyn.endercontainers.enderchest.context.PlayerContext;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class EnderChestListMenuTest {

    private EnderChestListMenu menu;

    @Mock
    private PlayerContext context;

    @Mock
    private EnderChest chest;

    @BeforeClass
    public static void setUpClass() throws TestInitializationException {
        TestHelper.setUpFiles();
    }

    @Before
    public void setUp() {
        UUID playerId = TestHelper.getPlayer().getUniqueId();

        when(this.context.getOwner()).thenReturn(playerId);
        when(this.context.getChest(0)).thenReturn(Optional.of(this.chest));
        when(this.chest.isAccessible()).thenReturn(true);
        this.menu = new EnderChestListMenu(this.context);
    }

    @Test
    public void prepare() {
        // check that chest has been prepared
        verify(this.chest).updateRowCount();

        // check first item
        ItemStack chestItem = this.menu.getInventory().getItem(0);
        assertThat(chestItem).isNotNull();
        assertThat(chestItem.getType()).isEqualTo(Material.LIME_STAINED_GLASS_PANE);
    }

    @Test
    public void click() {
        Player player = TestHelper.getPlayer();

        // cannot opened the chest by default
        this.menu.onClick(player, 0);
        verify(player).playSound(isNull(), eq(Sound.ENTITY_VILLAGER_NO), anyFloat(), anyFloat());

        // now test if player has perm
        when(this.context.openEnderchestInventory(player, 0)).thenReturn(true);
        this.menu.onClick(player, 0);
        verify(player).playSound(isNull(), eq(Sound.UI_BUTTON_CLICK), anyFloat(), anyFloat());
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

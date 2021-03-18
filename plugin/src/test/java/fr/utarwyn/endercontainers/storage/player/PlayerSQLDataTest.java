package fr.utarwyn.endercontainers.storage.player;

import fr.utarwyn.endercontainers.TestHelper;
import fr.utarwyn.endercontainers.database.DatabaseManager;
import fr.utarwyn.endercontainers.database.DatabaseSet;
import fr.utarwyn.endercontainers.enderchest.EnderChest;
import fr.utarwyn.endercontainers.storage.serialization.ItemSerializer;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class PlayerSQLDataTest {

    private static final String FAKE_DATA = "FAKE_DATA";

    private static final ConcurrentHashMap<Integer, ItemStack> CONTENTS = new ConcurrentHashMap<Integer, ItemStack>() {{
        put(14, new ItemStack(Material.DIRT, 2));
        put(26, new ItemStack(Material.DIAMOND, 34));
    }};

    private Player player;

    private PlayerSQLData data;

    @Mock
    private DatabaseManager databaseManager;

    @Mock
    private EnderChest chest;

    @Mock
    private ItemSerializer itemSerializer;

    @Before
    public void setUp() throws ReflectiveOperationException, IOException, SQLException {
        TestHelper.registerManagers(this.databaseManager);

        this.player = TestHelper.getPlayer();
        UUID uuid = this.player.getUniqueId();

        this.data = new PlayerSQLData(uuid, TestHelper.getPlugin(), this.itemSerializer);

        when(this.chest.getOwner()).thenReturn(uuid);
        when(this.itemSerializer.deserialize(FAKE_DATA)).thenReturn(CONTENTS);
        when(this.itemSerializer.serialize(CONTENTS)).thenReturn(FAKE_DATA);
        when(this.databaseManager.getEnderchestsOf(uuid)).thenReturn(Arrays.asList(
                createChestSet(1, 3, uuid, FAKE_DATA),
                createChestSet(10, 5, uuid, FAKE_DATA),
                createChestSet(5, 3, uuid, null)
        ));
    }

    @Test
    public void getEnderchestContents() throws SQLException {
        this.data.load();
        this.data.save();

        // Valid chests
        when(this.chest.getNum()).thenReturn(1);
        assertThat(this.data.getEnderchestContents(this.chest)).isNotEmpty().hasSize(2);
        when(this.chest.getNum()).thenReturn(10);
        assertThat(this.data.getEnderchestContents(this.chest)).isNotEmpty().hasSize(2);

        // Unknown/NULL chests
        when(this.chest.getNum()).thenReturn(2);
        assertThat(this.data.getEnderchestContents(this.chest)).isEmpty();
        when(this.chest.getNum()).thenReturn(5);
        assertThat(this.data.getEnderchestContents(this.chest)).isEmpty();

        // SQL error during loading, do not throw exception
        when(this.databaseManager.getEnderchestsOf(this.player.getUniqueId())).thenThrow(SQLException.class);
        this.data.load();
    }

    @Test
    public void getEnderchestRows() {
        this.data.load();

        // Valid chests
        when(this.chest.getNum()).thenReturn(1);
        assertThat(this.data.getEnderchestRows(this.chest)).isEqualTo(3);
        when(this.chest.getNum()).thenReturn(10);
        assertThat(this.data.getEnderchestRows(this.chest)).isEqualTo(5);

        // Unknown chests, default to 3
        when(this.chest.getNum()).thenReturn(2);
        assertThat(this.data.getEnderchestRows(this.chest)).isEqualTo(3);
    }

    @Test
    public void saveNewEnderchest() throws SQLException {
        // Insert a new enderchest
        when(this.chest.getNum()).thenReturn(12);
        when(this.chest.getRows()).thenReturn(6);
        when(this.chest.getContents()).thenReturn(CONTENTS);
        this.data.saveEnderchest(this.chest);
        verify(this.databaseManager).saveEnderchest(true, this.player.getUniqueId(), 12, 6, FAKE_DATA);

        // Try to update same enderchest but without content
        when(this.chest.getContents()).thenReturn(new ConcurrentHashMap<>());
        this.data.saveEnderchest(this.chest);
        verify(this.databaseManager).saveEnderchest(false, this.player.getUniqueId(), 12, 6, null);

        // SQL error during saving, do not throw exception
        doThrow(SQLException.class).when(this.databaseManager).saveEnderchest(
                eq(false), any(UUID.class), eq(12), eq(6), isNull()
        );
        this.data.saveEnderchest(this.chest);
    }

    @Test
    public void saveExistingEnderchest() throws SQLException {
        when(this.chest.getNum()).thenReturn(1);
        when(this.chest.getRows()).thenReturn(4);
        when(this.chest.getContents()).thenReturn(CONTENTS);

        this.data.load();
        this.data.saveEnderchest(this.chest);

        verify(this.databaseManager).saveEnderchest(false, this.player.getUniqueId(), 1, 4, FAKE_DATA);
    }

    private DatabaseSet createChestSet(int num, int rows, UUID owner, String contents) {
        DatabaseSet set = new DatabaseSet();
        set.setObject("id", num);
        set.setObject("num", num);
        set.setObject("owner", owner.toString());
        set.setObject("contents", contents);
        set.setObject("rows", rows);
        return set;
    }

}

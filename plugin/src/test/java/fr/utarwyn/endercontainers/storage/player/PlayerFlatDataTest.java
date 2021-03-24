package fr.utarwyn.endercontainers.storage.player;

import fr.utarwyn.endercontainers.TestHelper;
import fr.utarwyn.endercontainers.TestInitializationException;
import fr.utarwyn.endercontainers.enderchest.EnderChest;
import fr.utarwyn.endercontainers.storage.serialization.ItemSerializer;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PlayerFlatDataTest {

    private static final UUID TEST_UUID = UUID.fromString("06242f9a-6fcf-4504-b69a-9420da52ee9d");

    private static final String VALID = "VALID";

    private static final ConcurrentMap<Integer, ItemStack> ITEMS = new ConcurrentHashMap<Integer, ItemStack>() {{
        put(1, new ItemStack(Material.OAK_BOAT));
    }};

    private PlayerFlatData data;

    @Mock
    private EnderChest chest;

    @Mock
    private ItemSerializer itemSerializer;

    @BeforeClass
    public static void setUpClass() throws TestInitializationException {
        TestHelper.setUpFiles();
    }

    @Before
    public void setUp() throws TestInitializationException, IOException {
        this.data = new PlayerFlatData(TEST_UUID, TestHelper.getPlugin(), this.itemSerializer);
        when(this.itemSerializer.serialize(ITEMS)).thenReturn(VALID);
        when(this.itemSerializer.deserialize(VALID)).thenReturn(ITEMS);
    }

    @Test
    public void load() {
        assertThat(this.data.configuration).isNotNull();
    }

    @Test
    public void save() throws TestInitializationException {
        this.data.configuration.set("check", true);
        this.data.save();

        String filename = TEST_UUID.toString().replace("-", "") + ".yml";
        File file = new File(TestHelper.getPlugin().getDataFolder(), "data" + File.separator + filename);

        try (Scanner reader = new Scanner(file)) {
            assertThat(reader.hasNextLine()).isTrue();
            assertThat(reader.nextLine()).isEqualTo("check: true");
            assertThat(reader.hasNextLine()).isFalse();
        } catch (IOException e) {
            fail("player data file must be written on the disk");
        }
    }

    @Test
    public void getEnderchestContents() throws IOException {
        this.data.configuration.set("enderchests.1.contents", VALID);

        // valid chest with data
        when(this.chest.getNum()).thenReturn(1);
        assertThat(this.data.getEnderchestContents(this.chest)).isNotEmpty();
        verify(this.itemSerializer).deserialize(VALID);

        // unknown chest
        when(this.chest.getNum()).thenReturn(2);
        assertThat(this.data.getEnderchestContents(this.chest)).isEmpty();
    }

    @Test
    public void getEnderchestRows() {
        this.data.configuration.set("enderchests.1.rows", 5);

        // valid chest with row data
        when(this.chest.getNum()).thenReturn(1);
        assertThat(this.data.getEnderchestRows(this.chest)).isEqualTo(5);

        // unknown chest
        when(this.chest.getNum()).thenReturn(2);
        assertThat(this.data.getEnderchestRows(this.chest)).isEqualTo(3);
    }

    @Test
    public void saveEnderchest() {
        when(this.chest.getNum()).thenReturn(1);
        when(this.chest.getRows()).thenReturn(4);
        when(this.chest.getContents()).thenReturn(ITEMS);

        this.data.saveEnderchest(this.chest);

        String base = "enderchests.1.";
        assertThat(this.data.configuration.get(base + "rows")).isEqualTo(4);
        assertThat(this.data.configuration.get(base + "position")).isEqualTo(1);
        assertThat(this.data.configuration.get(base + "contents")).isEqualTo(VALID);
    }

}

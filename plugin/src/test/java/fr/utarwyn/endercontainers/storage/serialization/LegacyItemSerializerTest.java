package fr.utarwyn.endercontainers.storage.serialization;

import fr.utarwyn.endercontainers.TestHelper;
import fr.utarwyn.endercontainers.mock.EnchantmentMock;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static org.assertj.core.api.Assertions.assertThat;

public class LegacyItemSerializerTest {

    private ItemSerializer serializer;

    private Enchantment enchantment;

    @BeforeClass
    public static void setUpClass() {
        TestHelper.setUpServer();
    }

    @Before
    public void setUp() {
        this.serializer = new LegacyItemSerializer();
        this.enchantment = new EnchantmentMock("DAMAGE_ALL");
    }

    @Test
    public void serialize() throws IOException {
        ConcurrentMap<Integer, ItemStack> map = new ConcurrentHashMap<>();

        ItemStack log = new ItemStack(Material.OAK_LOG, 10);
        ItemMeta logMeta = log.getItemMeta();
        if (logMeta != null) {
            ((Damageable) logMeta).setDamage(5);
            logMeta.setDisplayName("AWESOME TEST LOG @#");
            logMeta.addEnchant(this.enchantment, 1, false);
            logMeta.setLore(Arrays.asList("first line", "second line"));
            log.setItemMeta(logMeta);
        }

        map.put(1, log);
        map.put(17, new ItemStack(Material.GRASS, 20));

        assertThat(this.serializer.serialize(map)).isNotNull()
                .isEqualTo("2;1#t@OAK_LOG:d@5:a@10:e@minecraft!damage_all@1:n@AWESOME=TEST=LOG=\\\\@\\\\#=" +
                        ":l@first line=second line=;17#t@GRASS:a@20:l@;");
    }

    @Test
    public void deserialize() throws IOException {
        ConcurrentMap<Integer, ItemStack> result = this.serializer.deserialize(
                "2;6#t@GRASS:d@2:e@DAMAGE_ALL@1:n@AMAZING=GRASS=" +
                        ":l@first line=second line=;23#t@OAK_LOG:a@7;"
        );

        ConcurrentMap<Integer, ItemStack> expected = new ConcurrentHashMap<>();

        ItemStack grass = new ItemStack(Material.GRASS, 1);
        ItemMeta grassMeta = grass.getItemMeta();
        if (grassMeta != null) {
            ((Damageable) grassMeta).setDamage(2);
            grassMeta.setDisplayName("AMAZING GRASS");
            grassMeta.addEnchant(this.enchantment, 1, false);
            grassMeta.setLore(Arrays.asList("first line", "second line"));
            grass.setItemMeta(grassMeta);
        }

        expected.put(6, grass);
        expected.put(23, new ItemStack(Material.OAK_LOG, 7));

        assertThat(result).isNotNull().isNotEmpty().hasSize(2)
                .containsExactlyEntriesOf(expected);
    }

}

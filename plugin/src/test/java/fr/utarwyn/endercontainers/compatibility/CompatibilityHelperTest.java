package fr.utarwyn.endercontainers.compatibility;

import fr.utarwyn.endercontainers.TestHelper;
import fr.utarwyn.endercontainers.compatibility.bukkit.BukkitArmorStandAdapter;
import fr.utarwyn.endercontainers.compatibility.nms.NMSArmorStandAdapter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CompatibilityHelperTest {

    @BeforeAll
    public static void setUpClass() {
        TestHelper.setUpServer();
    }

    @Test
    public void searchMaterial() {
        // null value
        try {
            CompatibilityHelper.searchMaterial(null);
            fail("must reject null values");
        } catch (NullPointerException ignored) {
        }

        // Prepare Bukkit unsafe
        when(Bukkit.getUnsafe().fromLegacy(Material.LEGACY_SKULL_ITEM)).thenReturn(Material.PLAYER_HEAD);

        // test with some values
        assertThat(CompatibilityHelper.searchMaterial("OAK_LOG")).isEqualTo(Material.OAK_LOG);
        assertThat(CompatibilityHelper.searchMaterial("SKULL_ITEM")).isEqualTo(Material.PLAYER_HEAD);
    }

    @Test
    public void searchSound() {
        // unknown value
        try {
            CompatibilityHelper.searchSound("UNKNOWN_1", "UNKNOWN_2");
            fail("must reject unknown values");
        } catch (IllegalArgumentException ignored) {
        }

        // one of provided values is valid
        assertThat(CompatibilityHelper.searchSound("BLOCK_CHEST_OPEN")).isEqualTo(Sound.BLOCK_CHEST_OPEN);
        assertThat(CompatibilityHelper.searchSound("BLOCK_CHEST_OPEN", "BLOCK_CHEST_CLOSE")).isEqualTo(Sound.BLOCK_CHEST_OPEN);
        assertThat(CompatibilityHelper.searchSound("CHEST_OPEN", "BLOCK_CHEST_OPEN", "BLOCK_CHEST_CLOSE")).isEqualTo(Sound.BLOCK_CHEST_OPEN);
    }

    @Test
    public void createBukkitArmorStandAdapter() {
        ServerVersion def = ServerVersion.get();
        TestHelper.overrideServerVersion(ServerVersion.V1_20);
        assertThat(CompatibilityHelper.createArmorStandAdapter()).isInstanceOf(BukkitArmorStandAdapter.class);
        TestHelper.overrideServerVersion(def);
    }

    @Test
    public void createNMSArmorStandAdapter() {
        ServerVersion def = ServerVersion.get();
        TestHelper.overrideServerVersion(ServerVersion.V1_8);
        assertThat(CompatibilityHelper.createArmorStandAdapter()).isInstanceOf(NMSArmorStandAdapter.class);
        TestHelper.overrideServerVersion(def);
    }

}

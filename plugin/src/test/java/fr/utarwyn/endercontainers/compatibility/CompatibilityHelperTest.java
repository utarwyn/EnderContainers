package fr.utarwyn.endercontainers.compatibility;

import fr.utarwyn.endercontainers.TestHelper;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CompatibilityHelperTest {

    @BeforeClass
    public static void setUpClass() {
        TestHelper.setUpServer();
    }

    @Test
    public void matchMaterial() {
        // null value
        try {
            CompatibilityHelper.matchMaterial(null);
            fail("must reject null values");
        } catch (NullPointerException ignored) {
        }

        // Prepare Bukkit unsafe
        when(Bukkit.getUnsafe().fromLegacy(Material.LEGACY_SKULL_ITEM)).thenReturn(Material.PLAYER_HEAD);

        // test with some values
        assertThat(CompatibilityHelper.matchMaterial("OAK_LOG")).isEqualTo(Material.OAK_LOG);
        assertThat(CompatibilityHelper.matchMaterial("SKULL_ITEM")).isEqualTo(Material.PLAYER_HEAD);
    }

}

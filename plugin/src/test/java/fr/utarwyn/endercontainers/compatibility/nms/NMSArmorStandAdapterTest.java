package fr.utarwyn.endercontainers.compatibility.nms;

import fr.utarwyn.endercontainers.TestHelper;
import fr.utarwyn.endercontainers.TestInitializationException;
import fr.utarwyn.endercontainers.hologram.HologramException;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class NMSArmorStandAdapterTest {

    private static final String TEXT = "text";

    private NMSArmorStandAdapter armorStandAdapter;

    @Mock
    private Player observer;

    @Mock
    private Location location;

    @BeforeAll
    public static void setUpClass() {
        TestHelper.setUpServer();
    }

    @BeforeEach
    public void setUp() {
        this.armorStandAdapter = new NMSArmorStandAdapter();
    }

    @Test
    public void spawnArmorStandFor() throws HologramException, ReflectiveOperationException, TestInitializationException {
        this.armorStandAdapter.spawnArmorStandFor(TestHelper.getPlugin(), this.observer, this.location, TEXT);
        verify(NMSHologramUtil.get()).spawnHologram(this.location, TEXT, this.observer);
    }

    @Test
    public void handleErrorWhenSpawningArmorStand() throws ReflectiveOperationException, TestInitializationException {
        doThrow(ReflectiveOperationException.class).when(NMSHologramUtil.get()).spawnHologram(this.location, TEXT, this.observer);
        try {
            this.armorStandAdapter.spawnArmorStandFor(TestHelper.getPlugin(), this.observer, this.location, TEXT);
            fail("should throw an exception");
        } catch (HologramException exception) {
            assertThat(exception.getMessage()).isEqualTo("cannot spawn hologram entity");
        }
    }

    @Test
    public void destroyArmorStandForOnlineObserver() throws HologramException, ReflectiveOperationException {
        doReturn(true).when(this.observer).isOnline();
        this.armorStandAdapter.destroyArmorStandFor(this.observer, 1);
        verify(NMSHologramUtil.get()).destroyEntity(1, this.observer);
    }

    @Test
    public void doNotDestroyArmorStandForOfflineObserver() throws HologramException, ReflectiveOperationException {
        doReturn(false).when(this.observer).isOnline();
        this.armorStandAdapter.destroyArmorStandFor(this.observer, 1);
        verify(NMSHologramUtil.get(), never()).destroyEntity(1, this.observer);
    }

    @Test
    public void handleErrorWhenDestroyingArmorStandFor() throws ReflectiveOperationException {
        doReturn(true).when(this.observer).isOnline();
        doThrow(ReflectiveOperationException.class).when(NMSHologramUtil.get()).destroyEntity(10, this.observer);
        try {
            this.armorStandAdapter.destroyArmorStandFor(this.observer, 10);
            fail("should throw an exception");
        } catch (HologramException exception) {
            assertThat(exception.getMessage()).isEqualTo("cannot destroy hologram entity");
        }
    }

}

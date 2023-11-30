package fr.utarwyn.endercontainers.hologram;

import fr.utarwyn.endercontainers.TestHelper;
import fr.utarwyn.endercontainers.TestInitializationException;
import fr.utarwyn.endercontainers.compatibility.nms.NMSHologramUtil;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class HologramTest {

    private static final String TITLE = "title";

    private static final Integer ENTITY_ID = 10;

    @Mock
    private Player observer;

    @Mock
    private Location location;

    @BeforeAll
    public static void setUpClass() throws TestInitializationException {
        TestHelper.setUpServer();
        TestHelper.getPlugin();
    }

    @Test
    public void spawn() throws HologramException, ReflectiveOperationException {
        new Hologram(this.observer, TITLE, this.location);
        verify(NMSHologramUtil.get()).spawnHologram(this.location, TITLE, this.observer);
    }

    @Test
    public void destroy() throws HologramException, ReflectiveOperationException {
        when(NMSHologramUtil.get().spawnHologram(this.location, TITLE, this.observer)).thenReturn(ENTITY_ID);
        Hologram h = new Hologram(this.observer, TITLE, this.location);
        h.destroy();
        verify(NMSHologramUtil.get()).destroyEntity(ENTITY_ID, this.observer);
    }

    @Test
    public void observerOnline() throws HologramException {
        Hologram hologram = new Hologram(this.observer, TITLE, this.location);

        when(this.observer.isOnline()).thenReturn(true);
        assertThat(hologram.isObserverOnline()).isTrue();
        when(this.observer.isOnline()).thenReturn(false);
        assertThat(hologram.isObserverOnline()).isFalse();
    }

    @Test
    public void spawnError() throws ReflectiveOperationException {
        when(NMSHologramUtil.get().spawnHologram(this.location, TITLE, this.observer))
                .thenThrow(ReflectiveOperationException.class);

        try {
            new Hologram(this.observer, TITLE, this.location);
            fail("spawn method must fail");
        } catch (HologramException ignored) {
        }
    }

    @Test
    public void destroyError() throws HologramException, ReflectiveOperationException {
        when(NMSHologramUtil.get().spawnHologram(this.location, TITLE, this.observer)).thenReturn(ENTITY_ID);
        doThrow(ReflectiveOperationException.class).when(NMSHologramUtil.get()).destroyEntity(ENTITY_ID, this.observer);

        Hologram h = new Hologram(this.observer, TITLE, this.location);

        try {
            h.destroy();
            fail("destroy method must fail");
        } catch (HologramException ignored) {
        }
    }

}

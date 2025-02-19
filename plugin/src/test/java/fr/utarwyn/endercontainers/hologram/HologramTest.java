package fr.utarwyn.endercontainers.hologram;

import fr.utarwyn.endercontainers.TestHelper;
import fr.utarwyn.endercontainers.TestInitializationException;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

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
    public void observerOnline() throws HologramException {
        Hologram hologram = new Hologram(this.observer, ENTITY_ID);

        when(this.observer.isOnline()).thenReturn(true);
        assertThat(hologram.isObserverOnline()).isTrue();
        when(this.observer.isOnline()).thenReturn(false);
        assertThat(hologram.isObserverOnline()).isFalse();
    }

}

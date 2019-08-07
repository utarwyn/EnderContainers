package fr.utarwyn.endercontainers;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.logging.Logger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class ManagersTest {

    private static AbstractManager mockedManager;

    @BeforeClass
    public static void beforeClass() {
        mockedManager = mock(AbstractManager.class);
    }

    @AfterClass
    public static void afterClass() {
        Managers.getInstances().clear();
    }

    @Test
    public void testRegister() {
        EnderContainers plugin = mock(EnderContainers.class);
        Logger logger = mock(Logger.class);
        when(plugin.getLogger()).thenReturn(logger);

        assertThat(Managers.getInstances()).isEmpty();

        // Verify a good registration
        AbstractManager manager = Managers.register(plugin, mockedManager.getClass());
        assertThat(manager).isNotNull();
        assertThat(Managers.getInstances()).isNotEmpty().hasSize(1);

        // A manager cannot be registered two times
        AbstractManager manager2 = Managers.register(plugin, mockedManager.getClass());
        assertThat(manager2).isNull();
        assertThat(Managers.getInstances()).hasSize(1);

        // Try to register directly an abstract class?
        AbstractManager manager3 = Managers.register(plugin, AbstractManager.class);
        assertThat(manager3).isNull();
        assertThat(Managers.getInstances()).hasSize(1);
    }

    @Test
    public void testGet() {
        assertThat(Managers.get(mockedManager.getClass()))
                .isNotNull()
                .isInstanceOf(mockedManager.getClass());

        assertThat(Managers.get(AbstractManager.class)).isNull();
    }

    @Test
    public void testReload() {
        assertThat(Managers.reload(mockedManager.getClass())).isTrue();
        assertThat(Managers.reload(AbstractManager.class)).isFalse();

        // Check if methods "load" and "unload" are called on a manager
        AbstractManager manager2 = mock(AbstractManager.class);
        Managers.getInstances().put(manager2.getClass(), manager2);

        Managers.reload(manager2.getClass());
        verify(manager2, times(1)).load();
        verify(manager2, times(1)).unload();
        Managers.getInstances().remove(manager2.getClass());
    }

}

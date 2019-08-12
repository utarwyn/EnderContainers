package fr.utarwyn.endercontainers;

import org.junit.Before;
import org.junit.Test;

import java.util.logging.Logger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class ManagersTest {

    private AbstractManager manager;

    @Before
    public void setUp() {
        this.manager = mock(AbstractManager.class);
        Managers.getInstances().clear();
    }

    @Test
    public void testRegister() {
        EnderContainers plugin = mock(EnderContainers.class);
        Logger logger = mock(Logger.class);
        when(plugin.getLogger()).thenReturn(logger);

        assertThat(Managers.getInstances()).isEmpty();

        // Verify a good registration
        AbstractManager manager1 = Managers.register(plugin, this.manager.getClass());
        assertThat(manager1).isNotNull();
        assertThat(Managers.getInstances()).isNotEmpty().hasSize(1);

        // A manager cannot be registered two times
        AbstractManager manager2 = Managers.register(plugin, this.manager.getClass());
        assertThat(manager2).isNull();
        assertThat(Managers.getInstances()).hasSize(1);

        // Try to register directly an abstract class?
        AbstractManager manager3 = Managers.register(plugin, AbstractManager.class);
        assertThat(manager3).isNull();
        assertThat(Managers.getInstances()).hasSize(1);
    }

    @Test
    public void testGet() {
        Managers.getInstances().put(this.manager.getClass(), this.manager);

        assertThat(Managers.get(this.manager.getClass())).isNotNull()
                .isInstanceOf(this.manager.getClass());

        assertThat(Managers.get(AbstractManager.class)).isNull();
    }

    @Test
    public void testReload() {
        Managers.getInstances().put(this.manager.getClass(), this.manager);

        assertThat(Managers.reload(this.manager.getClass())).isTrue();
        assertThat(Managers.reload(AbstractManager.class)).isFalse();

        // Check if methods "load" and "unload" are called on a manager
        verify(this.manager, times(1)).load();
        verify(this.manager, times(1)).unload();
    }

}

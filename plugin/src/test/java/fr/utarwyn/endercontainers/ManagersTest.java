package fr.utarwyn.endercontainers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class ManagersTest {

    @Mock
    private AbstractManager manager;

    @BeforeEach
    public void setUp() {
        Managers.instances.clear();
    }

    @Test
    public void register() throws TestInitializationException {
        EnderContainers plugin = TestHelper.getPlugin();

        assertThat(Managers.instances).isEmpty();

        // Verify a good registration
        AbstractManager manager1 = Managers.register(plugin, this.manager.getClass());
        assertThat(manager1).isNotNull();
        assertThat(Managers.instances).isNotEmpty().hasSize(1);

        // A manager cannot be registered two times
        AbstractManager manager2 = Managers.register(plugin, this.manager.getClass());
        assertThat(manager2).isNull();
        assertThat(Managers.instances).hasSize(1);
    }

    @Test
    public void get() {
        Managers.instances.put(this.manager.getClass(), this.manager);

        assertThat(Managers.get(this.manager.getClass())).isNotNull()
                .isInstanceOf(this.manager.getClass());

        try {
            Managers.get(AbstractManager.class);
            fail("An exception should be thrown when trying to get an unregistered manager.");
        } catch (NullPointerException e) {
            assertThat(e).hasMessage("class fr.utarwyn.endercontainers.AbstractManager instance is null!")
                    .hasNoCause();
        }
    }

    @Test
    public void reload() {
        Managers.instances.put(this.manager.getClass(), this.manager);

        assertThat(Managers.reload(this.manager.getClass())).isTrue();
        assertThat(Managers.reload(AbstractManager.class)).isFalse();

        // Check if methods "load" and "unload" are called on a manager
        verify(this.manager).load();
        verify(this.manager).unload();
    }

}

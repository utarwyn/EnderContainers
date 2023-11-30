package fr.utarwyn.endercontainers.compatibility;

import fr.utarwyn.endercontainers.TestHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static fr.utarwyn.endercontainers.compatibility.ServerVersion.*;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class ServerVersionTest {

    @BeforeEach
    public void setUpClass() {
        TestHelper.setUpServer();
    }

    @Test
    public void getVersion() {
        // Should retrieve the package of the Server class.
        // In testing env, the fake server is in a package named v1_15.
        assertThat(ServerVersion.getBukkitVersion()).isEqualTo("v1_15");
        assertThat(ServerVersion.get()).isEqualTo(V1_15);
    }

    @Test
    public void comparison() {
        // Equality
        assertThat(ServerVersion.is(V1_12)).isFalse();
        assertThat(ServerVersion.is(V1_15)).isTrue();
        assertThat(ServerVersion.is(V1_16)).isFalse();

        // Older
        assertThat(ServerVersion.isOlderThan(V1_12)).isFalse();
        assertThat(ServerVersion.isOlderThan(V1_15)).isFalse();
        assertThat(ServerVersion.isOlderThan(V1_16)).isTrue();

        // Newer
        assertThat(ServerVersion.isNewerThan(V1_12)).isTrue();
        assertThat(ServerVersion.isNewerThan(V1_15)).isFalse();
        assertThat(ServerVersion.isNewerThan(V1_16)).isFalse();
    }

}

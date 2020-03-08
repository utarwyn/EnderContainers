package fr.utarwyn.endercontainers.compatibility;

import fr.utarwyn.endercontainers.TestHelper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import static fr.utarwyn.endercontainers.compatibility.ServerVersion.*;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class ServerVersionTest {

    @Before
    public void setUpClass() {
        TestHelper.setUpServer();
    }

    @Test
    public void getVersion() {
        // Should retrieve the package of the Server class.
        // In testing env, the fake server is in a package named v1_12.
        assertThat(ServerVersion.getBukkitVersion()).isEqualTo("v1_12");
        assertThat(ServerVersion.get()).isEqualTo(V1_12);
    }

    @Test
    public void comparison() {
        // Equality
        assertThat(ServerVersion.is(V1_8)).isFalse();
        assertThat(ServerVersion.is(V1_12)).isTrue();
        assertThat(ServerVersion.is(V1_15)).isFalse();

        // Older
        assertThat(ServerVersion.isOlderThan(V1_8)).isFalse();
        assertThat(ServerVersion.isOlderThan(V1_12)).isFalse();
        assertThat(ServerVersion.isOlderThan(V1_15)).isTrue();

        // Newer
        assertThat(ServerVersion.isNewerThan(V1_8)).isTrue();
        assertThat(ServerVersion.isNewerThan(V1_12)).isFalse();
        assertThat(ServerVersion.isNewerThan(V1_15)).isFalse();
    }

}

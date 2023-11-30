package fr.utarwyn.endercontainers.compatibility.nms;

import fr.utarwyn.endercontainers.TestHelper;
import fr.utarwyn.endercontainers.compatibility.ServerVersion;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static fr.utarwyn.endercontainers.TestHelper.overrideServerVersion;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class NMSUtilTest {

    @BeforeAll
    public static void setUpClass() {
        TestHelper.setUpServer();
    }

    @Test
    public void getNMSClass() {
        try {
            NMSUtil.getNMSClass("Fake", "package1_17");
            fail("class retrieving must fail");
        } catch (ClassNotFoundException e) {
            assertThat(e.getMessage()).isEqualTo("net.minecraft.server.v1_15.Fake");
        }
    }

    @Test
    public void getNMSDynamicMethod() throws ReflectiveOperationException {
        ServerVersion defVersion = ServerVersion.get();
        overrideServerVersion(ServerVersion.V1_8);
        assertThat(NMSUtil.getNMSDynamicMethod(String.class, "toLowerCase", "toUpperCase").invoke("TeSt")).isEqualTo("test");
        overrideServerVersion(ServerVersion.V1_18);
        assertThat(NMSUtil.getNMSDynamicMethod(String.class, "toLowerCase", "toUpperCase").invoke("TeSt")).isEqualTo("TEST");
        overrideServerVersion(defVersion);
    }

}

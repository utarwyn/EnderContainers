package fr.utarwyn.endercontainers.configuration;

import fr.utarwyn.endercontainers.TestHelper;
import fr.utarwyn.endercontainers.TestInitializationException;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class FilesTest {

    @BeforeClass
    public static void setUpClass() throws TestInitializationException {
        TestHelper.setUpFiles();
    }

    @Test
    public void reload() throws ConfigLoadingException, TestInitializationException {
        assertThat(Files.getConfiguration()).isNotNull();
        assertThat(Files.getLocale()).isNotNull();
        Files.reload(TestHelper.getPlugin());
    }

}

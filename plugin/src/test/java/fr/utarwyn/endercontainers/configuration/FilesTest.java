package fr.utarwyn.endercontainers.configuration;

import fr.utarwyn.endercontainers.TestHelper;
import fr.utarwyn.endercontainers.TestInitializationException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class FilesTest {

    @BeforeAll
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

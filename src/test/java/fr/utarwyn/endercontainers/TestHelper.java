package fr.utarwyn.endercontainers;

import org.bukkit.Bukkit;
import org.bukkit.Server;

import java.util.logging.Logger;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Helper class for testing purposes.
 *
 * @author Utarwyn
 * @since 2.2.0
 */
public class TestHelper {

    private static boolean serverReady = false;

    private TestHelper() {

    }

    /**
     * Setup a mocked version of the Bukkit server.
     */
    public static synchronized void setUpServer() {
        if (!serverReady) {
            Server server = mock(Server.class);
            Logger logger = mock(Logger.class);

            when(server.getLogger()).thenReturn(logger);
            Bukkit.setServer(server);
            serverReady = true;
        }
    }

}

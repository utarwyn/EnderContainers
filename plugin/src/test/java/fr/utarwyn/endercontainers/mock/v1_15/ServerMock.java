package fr.utarwyn.endercontainers.mock.v1_15;

import org.bukkit.Server;
import org.bukkit.command.CommandMap;

/**
 * Fake server interface used for unit tests.
 */
public interface ServerMock extends Server {

    CommandMap getCommandMap();

}

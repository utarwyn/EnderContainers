package fr.utarwyn.endercontainers.command;

import fr.utarwyn.endercontainers.AbstractManager;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.PluginCommand;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

/**
 * A manager to handle commands of the plugin.
 *
 * @author Utarwyn <maxime.malgorn@laposte.net>
 * @since 2.2.0
 */
public class CommandManager extends AbstractManager {

    /**
     * The Bukkit command map retrieved with reflection
     */
    private CommandMap cachedCommandMap;

    /**
     * Register and initialize needed commands for the plugin.
     */
    public void registerCommands() {
        try {
            this.register(new MainCommand(this.plugin));
            this.register(new EnderchestCommand());
        } catch (ReflectiveOperationException e) {
            this.logger.log(Level.SEVERE, "Cannot instanciate a command class", e);
        }
    }

    /**
     * Unregister an exisiting command registered from another plugin.
     * This method also removes all aliases of the command.
     *
     * @param command Command to unregister completely from the server.
     */
    public void unregister(PluginCommand command) {
        try {
            CommandMap commandMap = this.getCommandMap();
            Map<String, Command> commands = this.getRegisteredCommands(commandMap);

            commands.remove(command.getName());
            for (String alias : command.getAliases()) {
                if (commands.containsKey(alias) && commands.get(alias).toString().contains(command.getName())) {
                    commands.remove(alias);
                }
            }
        } catch (ReflectiveOperationException e) {
            this.logger.log(Level.SEVERE, String.format(
                    "Cannot unregister the command %s from the server!", command.getName()
            ), e);
        }
    }

    /**
     * Register an abstract command directly inside the server's command map.
     * This method is called by the AsbtractCommand class.
     *
     * @param command command to register inside the Bukkit server
     */
    private void register(AbstractCommand command) throws ReflectiveOperationException {
        CommandMap commandMap = getCommandMap();

        if (commandMap != null) {
            commandMap.register("endercontainers", command);
        }
    }

    /**
     * This method returns the command map of the server!
     *
     * @return The Bukkit internal Command map
     * @throws ReflectiveOperationException "commandMap" field cannot be found
     */
    private CommandMap getCommandMap() throws ReflectiveOperationException {
        // Get the command map of the server first!
        if (cachedCommandMap == null) {
            Server server = this.plugin.getServer();
            Field fMap = server.getClass().getDeclaredField("commandMap");

            fMap.setAccessible(true);
            cachedCommandMap = (CommandMap) fMap.get(server);
            fMap.setAccessible(false);
        }

        return cachedCommandMap;
    }

    /**
     * This method returns a map with all commands registered in the server.
     *
     * @param commandMap command map in which commands are stored
     * @return map of all registered commands
     * @throws ReflectiveOperationException method "getKnownCommands" cannot be found
     */
    private HashMap<String, Command> getRegisteredCommands(CommandMap commandMap)
            throws ReflectiveOperationException {
        HashMap<String, Command> knownCmds;

        try { // 1.8 -> 1.12
            Field fKownCmds = commandMap.getClass().getDeclaredField("knownCommands");
            fKownCmds.setAccessible(true);
            knownCmds = (HashMap<String, Command>) fKownCmds.get(commandMap);
            fKownCmds.setAccessible(false);
        } catch (NoSuchFieldException ex) { // 1.13+
            Method m = commandMap.getClass().getDeclaredMethod("getKnownCommands");
            knownCmds = (HashMap<String, Command>) m.invoke(commandMap);
        }

        return knownCmds;
    }

}

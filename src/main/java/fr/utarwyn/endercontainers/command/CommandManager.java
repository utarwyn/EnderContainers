package fr.utarwyn.endercontainers.command;

import fr.utarwyn.endercontainers.AbstractManager;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.SimpleCommandMap;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
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
            this.register(EnderchestCommand.class);
            this.register(MainCommand.class);
        } catch (IllegalAccessException | InstantiationException | NoSuchMethodException |
                InvocationTargetException | NoSuchFieldException e) {
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
        } catch (NoSuchFieldException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            this.logger.log(Level.SEVERE, "Cannot unregister the command " + command.getName() + " from the server!", e);
        }
    }

    /**
     * Register an abstract command directly inside the server's command map.
     * This method is called by the AsbtractCommand class.
     *
     * @param commandClass Class of the command to register inside the Bukkit server
     */
    private void register(Class<? extends AbstractCommand> commandClass) throws IllegalAccessException,
            InstantiationException, NoSuchFieldException, NoSuchMethodException, InvocationTargetException {
        CommandMap commandMap = getCommandMap();

        if (commandMap != null) {
            commandMap.register("endercontainers", commandClass.getDeclaredConstructor().newInstance());
        }
    }

    /**
     * This method returns the command map of the server!
     *
     * @return The Bukkit internal Command map
     * @throws NoSuchFieldException "commandMap" field cannot be found
     * @throws IllegalAccessException cannot access to the field "commandMap"
     */
    private CommandMap getCommandMap() throws NoSuchFieldException, IllegalAccessException {
        // Get the command map of the server first!
        if (cachedCommandMap == null) {
            Server server = this.plugin.getServer();
            Field fMap = server.getClass().getDeclaredField("commandMap");

            fMap.setAccessible(true);
            cachedCommandMap = (SimpleCommandMap) fMap.get(server);
            fMap.setAccessible(false);
        }

        return cachedCommandMap;
    }

    /**
     * This method returns a map with all commands registered in the server.
     *
     * @param commandMap command map in which commands are stored
     * @return map of all registered commands
     * @throws NoSuchMethodException     method "getKnownCommands" cannot be found
     * @throws InvocationTargetException cannot invoke the method "getKnownCommands"
     * @throws IllegalAccessException    cannot access to a private field/method
     */
    private HashMap<String, Command> getRegisteredCommands(CommandMap commandMap)
            throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
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

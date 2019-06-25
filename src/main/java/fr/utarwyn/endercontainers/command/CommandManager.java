package fr.utarwyn.endercontainers.command;

import fr.utarwyn.endercontainers.AbstractManager;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.SimpleCommandMap;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
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
        } catch (IllegalAccessException | InstantiationException e) {
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
        CommandMap commandMap = getCommandMap();

        if (commandMap == null) {
            return;
        }

        try {
            Field fKownCmds;
            HashMap<String, Command> knownCmds;

            try {
                fKownCmds = commandMap.getClass().getDeclaredField("knownCommands");
            } catch (NoSuchFieldException ex) {
                fKownCmds = null;
            }

            if (fKownCmds != null) { // Old versions
                fKownCmds.setAccessible(true);
                knownCmds = (HashMap<String, Command>) fKownCmds.get(commandMap);
                fKownCmds.setAccessible(false);
            } else { // For 1.13 servers
                Method m = commandMap.getClass().getDeclaredMethod("getKnownCommands");
                knownCmds = (HashMap<String, Command>) m.invoke(commandMap);
            }

            knownCmds.remove(command.getName());
            for (String alias : command.getAliases()) {
                if (knownCmds.containsKey(alias) && knownCmds.get(alias).toString().contains(command.getName())) {
                    knownCmds.remove(alias);
                }
            }
        } catch (Exception ex) {
            this.logger.log(Level.SEVERE, "Cannot unregister the command " + command.getName() + " from the server!", ex);
        }
    }

    /**
     * Register an abstract command directly inside the server's command map.
     * This method is called by the AsbtractCommand class.
     *
     * @param commandClass Class of the command to register inside the Bukkit server
     */
    private void register(Class<? extends AbstractCommand> commandClass) throws IllegalAccessException, InstantiationException {
        CommandMap commandMap = getCommandMap();

        if (commandMap != null) {
            commandMap.register("endercontainers", commandClass.newInstance());
        }
    }

    /**
     * This method returns the command map of the server!
     *
     * @return The Bukkit internal Command map
     */
    private CommandMap getCommandMap() {
        // Get the command map of the server first!
        if (cachedCommandMap == null) {
            try {
                Server server = this.plugin.getServer();
                Field fMap = server.getClass().getDeclaredField("commandMap");

                fMap.setAccessible(true);
                cachedCommandMap = (SimpleCommandMap) fMap.get(server);
                fMap.setAccessible(false);
            } catch (Exception ex) {
                this.logger.log(Level.SEVERE, "Cannot fetch the command map from the server!", ex);
                return null;
            }
        }

        return cachedCommandMap;
    }

}

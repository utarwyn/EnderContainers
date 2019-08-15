package fr.utarwyn.endercontainers.command;

import fr.utarwyn.endercontainers.EnderContainers;
import fr.utarwyn.endercontainers.configuration.Files;
import fr.utarwyn.endercontainers.util.PluginMsg;
import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.util.StringUtil;

import java.util.*;

/**
 * Represents a command of the plugin.
 *
 * @author Utarwyn
 * @since 1.0.0
 */
public abstract class AbstractCommand extends Command implements TabCompleter, CommandExecutor, Listener {

    /**
     * Prefix for all command permissions of the plugin
     */
    private static final String PERM_PREFIX = "endercontainers.";

    /**
     * Permission needed to type the command (for a player)
     */
    private String permission;

    /**
     * List all parameters of this command
     */
    private List<Parameter> parameters;

    /**
     * List all sub-commands of this command
     */
    private List<AbstractCommand> subCommands;

    /**
     * List all arguments for this command
     */
    private List<String> args;

    /**
     * Cursor for the next argument count
     */
    private int nextArg;

    /**
     * Construct a command!
     *
     * @param name    Name of ths command
     * @param aliases Aliases of the command
     */
    public AbstractCommand(String name, String... aliases) {
        super(name);

        this.parameters = new ArrayList<>();
        this.subCommands = new ArrayList<>();

        // Add all aliases
        Collections.addAll(this.getAliases(), aliases);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command == null || sender == null || !command.getName().equalsIgnoreCase(getName())) return false;

        // Have we sub-commands for this command?
        if (args.length > 0)
            for (AbstractCommand subCommand : this.subCommands)
                if (subCommand.getName().equalsIgnoreCase(args[0]) || subCommand.getAliases().contains(args[0]))
                    return subCommand.onCommand(sender, subCommand, label, Arrays.copyOfRange(args, 1, args.length));

        // Check argument count
        if (!this.checkArgLength(args.length)) {
            sender.sendMessage(EnderContainers.PREFIX + ChatColor.RED + Files.getLocale().getCmdWrongArgumentCount());
            return true;
        }

        // Check also each argument
        int i = 0;
        for (Parameter param : this.parameters) {
            if (i < args.length && !param.checkValue(args[i])) {
                sender.sendMessage(EnderContainers.PREFIX + ChatColor.RED +
                        Files.getLocale().getCmdInvalidParameter().replace("%param%", args[i]));
                return true;
            }

            i++;
        }

        // Save all used arguments in memory
        this.args = Arrays.asList(args);

        // Run the command execution for the appropriated entity
        if (sender instanceof Player) {
            Player player = (Player) sender;

            // Player permission
            if (!this.hasRequiredPermission(player)) {
                PluginMsg.accessDenied(sender);
                return true;
            }

            this.nextArg = 0;
            this.perform(sender);

            this.nextArg = 0;
            this.performPlayer(player);
        } else if (sender instanceof ConsoleCommandSender) {
            this.nextArg = 0;
            this.perform(sender);

            this.nextArg = 0;
            this.performConsole(sender);
        }

        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
        List<String> customCompletions = new ArrayList<>();

        // Check sub-commands completions first
        if (args.length > 0)
            for (AbstractCommand subCommand : this.subCommands)
                if (subCommand.getName().equalsIgnoreCase(args[0]) || subCommand.getAliases().contains(args[0]))
                    return subCommand.tabComplete(sender, alias, Arrays.copyOfRange(args, 1, args.length));

        // Now check completions of this command
        if (!this.subCommands.isEmpty() && args.length > 0) {
            String lastWord = args[args.length - 1];
            List<String> matchedCommands = new ArrayList<>();
            Iterator<AbstractCommand> commandIterator = this.subCommands.iterator();

            AbstractCommand command;
            String cmdName;

            while (true) {
                if (!commandIterator.hasNext()) {
                    matchedCommands.sort(String.CASE_INSENSITIVE_ORDER);
                    customCompletions = matchedCommands;
                    break;
                }

                command = commandIterator.next();
                cmdName = command.getName();

                if (StringUtil.startsWithIgnoreCase(cmdName, lastWord)) {
                    matchedCommands.add(cmdName);
                }
            }
        }

        // Try to add into the list all completions for the current paramter
        int nbCurParam = args.length - 1;
        if (nbCurParam >= 0 && nbCurParam < parameters.size()) {
            Parameter<?> param = parameters.get(nbCurParam);
            boolean hasAccess = !(sender instanceof Player) || this.hasRequiredPermission((Player) sender);

            if (param != null && hasAccess) {
                String lastWord = args[args.length - 1];
                List<String> completions = param.getCompletions();

                if (completions != null) {
                    // Don't forget to sort all completions :)
                    Iterator<String> completionsIterator = completions.iterator();
                    List<String> matchedCompletions = new ArrayList<>();
                    String completion;

                    while (true) {
                        if (!completionsIterator.hasNext()) {
                            matchedCompletions.sort(String.CASE_INSENSITIVE_ORDER);
                            customCompletions.addAll(matchedCompletions);
                            break;
                        }

                        completion = completionsIterator.next();

                        if (StringUtil.startsWithIgnoreCase(completion, lastWord)) {
                            matchedCompletions.add(completion);
                        }
                    }
                } else {
                    customCompletions.addAll(super.tabComplete(sender, alias, args));
                }
            }
        }

        return customCompletions;
    }

    @Override
    public boolean execute(CommandSender sender, String commandLabel, String[] args) {
        return this.onCommand(sender, this, commandLabel, args);
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] strings) {
        return this.tabComplete(commandSender, s, strings);
    }

    @Override
    public void setPermission(String permission) {
        this.permission = permission;
    }

    protected void addParameter(Parameter parameter) {
        this.parameters.add(parameter);
    }

    protected void addSubCommand(AbstractCommand command) {
        this.subCommands.add(command);
    }

    protected void sendTo(CommandSender sender, String message) {
        sender.sendMessage(EnderContainers.PREFIX + message);
    }

    protected <T> T readArg() {
        return this.readArgAt(this.nextArg);
    }

    protected <T> T readArgOrDefault(T def) {
        return this.readArgAtOrDefault(this.nextArg, def);
    }

    protected <T> T readArgAt(int idx) {
        if (this.args.size() <= idx) {
            throw new IllegalArgumentException(idx + " is not a valid arguments count! Registered arguments count: " + this.args.size());
        }

        return this.readArgument(idx);
    }

    protected <T> T readArgAtOrDefault(int idx, T def) {
        return this.args.size() > idx ? this.readArgument(idx) : def;
    }

    /**
     * Check if a player has the permission to perform the command.
     *
     * @param player player to check
     * @return true if the player has the access
     */
    private boolean hasRequiredPermission(Player player) {
        return this.permission == null || player.hasPermission(PERM_PREFIX + this.permission);
    }

    /**
     * Check if the argument count is good or not.
     *
     * @param n argument count used by the sender
     * @return true if the argument count is correct
     */
    private boolean checkArgLength(int n) {
        return n >= this.parameters.stream().filter(Parameter::isNeeded).count();
    }

    private <T> T readArgument(int idx) {
        this.nextArg = idx + 1;

        Parameter<T> parameter = this.parameters.get(idx);
        return parameter.convertValue(this.args.get(idx));
    }

    public abstract void perform(CommandSender sender);

    public abstract void performPlayer(Player player);

    public abstract void performConsole(CommandSender sender);

}

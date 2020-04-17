package fr.utarwyn.endercontainers.command;

import fr.utarwyn.endercontainers.configuration.LocaleKey;
import fr.utarwyn.endercontainers.util.PluginMsg;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

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
        if (args.length > 0) {
            AbstractCommand subCommand = this.subCommands.stream()
                    .filter(c -> c.getName().equalsIgnoreCase(args[0]) || c.getAliases().contains(args[0]))
                    .findFirst().orElse(null);

            if (subCommand != null) {
                return subCommand.onCommand(
                        sender, subCommand, label,
                        Arrays.copyOfRange(args, 1, args.length)
                );
            }
        }

        // Check argument count
        if (!this.checkArgLength(args.length)) {
            PluginMsg.errorMessageWithPrefix(sender, LocaleKey.ERR_CMD_ARG_COUNT);
            return true;
        }

        // Check also each argument
        int i = 0;
        for (Parameter<?> param : this.parameters) {
            if (i < args.length && !param.checkValue(args[i])) {
                PluginMsg.errorMessageWithPrefix(sender, LocaleKey.ERR_CMD_INVALID_PARAM,
                        Collections.singletonMap("param", args[i]));
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
        List<String> autocompletions = new ArrayList<>();

        // No argument, no auto-completion.
        if (args.length == 0) return autocompletions;

        // Check sub-commands completions first
        for (AbstractCommand subCommand : this.subCommands) {
            if (subCommand.getName().equalsIgnoreCase(args[0]) || subCommand.getAliases().contains(args[0])) {
                return subCommand.tabComplete(sender, alias, Arrays.copyOfRange(args, 1, args.length));
            }
        }

        int index = args.length - 1;
        String lastWord = args[index];

        // Add sub commands in auto-completion
        if (!this.subCommands.isEmpty()) {
            List<String> commandNames = this.subCommands.stream().map(AbstractCommand::getName).collect(Collectors.toList());
            autocompletions.addAll(this.matchCompletions(lastWord, commandNames));
        }

        // Add parameters' auto-completion
        if (index < parameters.size()) {
            Parameter<?> param = parameters.get(index);
            boolean hasAccess = !(sender instanceof Player) || this.hasRequiredPermission((Player) sender);

            if (hasAccess) {
                if (param.isCustomCompletions()) {
                    autocompletions.addAll(this.matchCompletions(lastWord, param.getCompletions()));
                } else {
                    // If the parameter does not have auto-completion, use the default one (player list)
                    autocompletions.addAll(super.tabComplete(sender, alias, args));
                }
            }
        }

        return autocompletions;
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

    /**
     * Method called when an entity performed this command.
     *
     * @param sender entity whiches have performed the command
     */
    public void perform(CommandSender sender) {
        // Not implemented by default
    }

    /**
     * Method called when a player performed this command.
     *
     * @param player player whiches have performed the command
     */
    public void performPlayer(Player player) {
        // Not implemented by default
    }

    /**
     * Method called when the console performed this command.
     *
     * @param sender the server console as a sender
     */
    public void performConsole(CommandSender sender) {
        // Not implemented by default
    }

    protected void addParameter(Parameter parameter) {
        this.parameters.add(parameter);
    }

    protected void addSubCommand(AbstractCommand command) {
        this.subCommands.add(command);
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

    /**
     * Read an argument at a specific index.
     *
     * @param idx index where to read the argument
     * @param <T> type of argument to read
     * @return converted read value
     */
    private <T> T readArgument(int idx) {
        this.nextArg = idx + 1;

        Parameter<T> parameter = this.parameters.get(idx);
        return parameter.convertValue(this.args.get(idx));
    }

    /**
     * Extract from a list and sort auto-completions that begin with a sent argument.
     *
     * @param argument    argument sent by a user
     * @param completions list of initial auto-completions to check
     * @return sorted list of extracted auto-completions
     */
    private List<String> matchCompletions(String argument, List<String> completions) {
        return completions.stream()
                .filter(completion -> StringUtil.startsWithIgnoreCase(completion, argument))
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .collect(Collectors.toList());
    }

}

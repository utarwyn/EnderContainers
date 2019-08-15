package fr.utarwyn.endercontainers.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Represents a command parameter.
 *
 * @author Utarwyn
 * @since 2.2.0
 */
public class Parameter<T> {

    /**
     * Function used to convert the sent argument to the needed type
     */
    private Function<String, T> converter;

    /**
     * List of elements sent to users who want autocompletion on their argument
     */
    private List<T> completions;

    /**
     * Stores if this parameter is needed to perform a command
     */
    private boolean needed;

    /**
     * Construct a new parameter with a specific converter.
     *
     * @param converter function to convert an argument
     */
    private Parameter(Function<String, T> converter) {
        this.completions = new ArrayList<>();
        this.converter = converter;
        this.needed = true;
    }

    /**
     * An integer parameter
     */
    public static Parameter<Integer> INT() {
        return new Parameter<>(Integer::new);
    }

    /**
     * A string parameter
     */
    public static Parameter<String> STRING() {
        return new Parameter<>(String::new);
    }

    /**
     * Return true if this parameter is needed to perform a command.
     *
     * @return true if this parameter is not optional
     */
    public boolean isNeeded() {
        return this.needed;
    }

    /**
     * Use players names to autocomplete argument for this parameter.
     *
     * @return this parameter
     */
    public Parameter<T> withPlayersCompletions() {
        this.completions = null;
        return this;
    }

    /**
     * Define custom completions for this parameter.
     *
     * @param completions list of elements to send to a user who wants autocompletion
     * @return this parameter
     */
    @SafeVarargs
    public final Parameter<T> withCustomCompletions(T... completions) {
        this.completions = Arrays.asList(completions);
        return this;
    }

    /**
     * Sets this parameter as an optional one.
     *
     * @return this parameter
     */
    public Parameter<T> optional() {
        this.needed = false;
        return this;
    }

    /**
     * Check if a value matches the type of this parameter.
     *
     * @param value value to test
     * @return true if the value has the same type, false otherwise
     */
    public boolean checkValue(String value) {
        try {
            this.convertValue(value);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    /**
     * Get a list of completions to propose to users.
     *
     * @return list of elements for the autocompletion
     */
    List<String> getCompletions() {
        if (this.completions == null) {
            return null;
        } else {
            return this.completions.stream().map(T::toString).collect(Collectors.toList());
        }
    }

    /**
     * Convert a string to the type of the parameter.
     *
     * @param value value to convert
     * @return converted value
     */
    T convertValue(String value) {
        return this.converter.apply(value);
    }

}

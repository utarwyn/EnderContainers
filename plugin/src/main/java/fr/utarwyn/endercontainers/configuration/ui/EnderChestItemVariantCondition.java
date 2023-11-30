package fr.utarwyn.endercontainers.configuration.ui;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.BiPredicate;

public class EnderChestItemVariantCondition {

    protected final Key key;

    @Nullable
    protected final Operator operator;

    @Nullable
    protected final Double value;

    public EnderChestItemVariantCondition(String condition) {
        String[] parts = condition.split(" ");
        this.key = computeKey(condition, parts[0]);
        this.operator = parts.length > 1 ? computeOperator(condition, parts[1]) : null;
        this.value = parts.length > 2 ? computeValue(condition, parts[2]) : null;
    }

    private static Key computeKey(String condition, String conditionKey) {
        return Arrays.stream(Key.values())
                .filter(key -> conditionKey.equalsIgnoreCase(key.name()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(String.format(
                        "invalid condition key %s in condition `%s`",
                        conditionKey, condition
                )));
    }

    private static Operator computeOperator(String condition, String conditionOperator) {
        return Arrays.stream(Operator.values())
                .filter(op -> conditionOperator.equals(op.representation))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(String.format(
                        "invalid condition operator %s in condition `%s`",
                        conditionOperator, condition
                )));
    }

    private static Double computeValue(String condition, String conditionValue) {
        double factor = 1;
        if (conditionValue.endsWith("%")) {
            conditionValue = conditionValue.replace("%", "");
            factor = 0.01;
        }
        try {
            return Double.parseDouble(conditionValue) * factor;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(String.format(
                    "invalid condition value %s in condition `%s`",
                    conditionValue, condition
            ), e);
        }
    }

    public boolean isValidUsingOperator(double value) {
        if (this.operator != null && this.value != null) {
            return this.operator.checker.test(value, this.value);
        } else {
            return false;
        }
    }

    public Key getKey() {
        return this.key;
    }

    public enum Key {
        FILLING,
        INACCESSIBLE,
        NUMBER,
    }

    public enum Operator {
        EQUAL("=", Objects::equals),
        GREATER_OR_EQUAL(">=", (v1, v2) -> v1 >= v2),
        GREATER(">", (v1, v2) -> v1 > v2),
        LOWER_OR_EQUAL("<=", (v1, v2) -> v1 <= v2),
        LOWER("<", (v1, v2) -> v1 < v2),
        ;

        private final String representation;

        private final BiPredicate<Double, Double> checker;

        Operator(String representation, BiPredicate<Double, Double> checker) {
            this.representation = representation;
            this.checker = checker;
        }
    }

}

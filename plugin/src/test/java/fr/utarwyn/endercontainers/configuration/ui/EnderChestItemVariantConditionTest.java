package fr.utarwyn.endercontainers.configuration.ui;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;

public class EnderChestItemVariantConditionTest {

    @Test
    public void createConditionWithoutOperator() {
        EnderChestItemVariantCondition condition = new EnderChestItemVariantCondition("inaccessible");
        assertThat(condition).isNotNull();
        assertThat(condition.getKey()).isEqualTo(EnderChestItemVariantCondition.Key.INACCESSIBLE);
        assertThat(condition.operator).isNull();
        assertThat(condition.value).isNull();
    }

    @Test
    public void createConditionWithOperator() {
        EnderChestItemVariantCondition condition = new EnderChestItemVariantCondition("number = 3");
        assertThat(condition).isNotNull();
        assertThat(condition.getKey()).isEqualTo(EnderChestItemVariantCondition.Key.NUMBER);
        assertThat(condition.operator).isEqualTo(EnderChestItemVariantCondition.Operator.EQUAL);
        assertThat(condition.value).isEqualTo(3.0);
    }

    @Test
    public void createConditionWithOperatorAndPercentValue() {
        EnderChestItemVariantCondition condition = new EnderChestItemVariantCondition("filling >= 90%");
        assertThat(condition).isNotNull();
        assertThat(condition.getKey()).isEqualTo(EnderChestItemVariantCondition.Key.FILLING);
        assertThat(condition.operator).isEqualTo(EnderChestItemVariantCondition.Operator.GREATER_OR_EQUAL);
        assertThat(condition.value).isEqualTo(0.9);
    }

    @Test
    public void invalidCondition() {
        assertThat(assertThrows(IllegalArgumentException.class, () -> new EnderChestItemVariantCondition("fake")).getMessage())
                .isEqualTo("invalid condition key fake in condition `fake`");
        assertThat(assertThrows(IllegalArgumentException.class, () -> new EnderChestItemVariantCondition("filling >> 5%")).getMessage())
                .isEqualTo("invalid condition operator >> in condition `filling >> 5%`");
        assertThat(assertThrows(IllegalArgumentException.class, () -> new EnderChestItemVariantCondition("filling = wrong")).getMessage())
                .isEqualTo("invalid condition value wrong in condition `filling = wrong`");
    }

    @Test
    public void isValidUsingOperator() {
        // Without operator or value
        EnderChestItemVariantCondition condition = new EnderChestItemVariantCondition("inaccessible");
        assertThat(condition).isNotNull();
        assertThat(condition.isValidUsingOperator(0)).isFalse();

        // Equal
        condition = new EnderChestItemVariantCondition("number = 3");
        assertThat(condition).isNotNull();
        assertThat(condition.isValidUsingOperator(0)).isFalse();
        assertThat(condition.isValidUsingOperator(3.1)).isFalse();
        assertThat(condition.isValidUsingOperator(3)).isTrue();

        // Greater or than
        condition = new EnderChestItemVariantCondition("filling >= 50%");
        assertThat(condition).isNotNull();
        assertThat(condition.isValidUsingOperator(0.0)).isFalse();
        assertThat(condition.isValidUsingOperator(0.4)).isFalse();
        assertThat(condition.isValidUsingOperator(0.5)).isTrue();
        assertThat(condition.isValidUsingOperator(1)).isTrue();
    }

}

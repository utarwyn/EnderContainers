package fr.utarwyn.endercontainers.command;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

public class ParameterTest {

    @Test
    public void testStaticParameters() {
        assertThat(Parameter.integer()).isNotEqualTo(Parameter.integer());
        assertThat(Parameter.string()).isNotEqualTo(Parameter.string());
    }

    @Test
    public void testIsNeeded() {
        Parameter<Integer> parameter = Parameter.integer();
        assertThat(parameter.isNeeded()).isTrue();
    }

    @Test
    public void testIsCustomCompletions() {
        Parameter<Integer> parameter = Parameter.integer();
        assertThat(parameter.isCustomCompletions()).isTrue();
    }

    @Test
    public void testWithPlayersCompletions() {
        Parameter<String> parameter = Parameter.string();

        assertThat(parameter.isCustomCompletions()).isTrue();
        assertThat(parameter.withPlayersCompletions()).isEqualTo(parameter);
        assertThat(parameter.getCompletions()).isNotNull().isEmpty();
        assertThat(parameter.isCustomCompletions()).isFalse();
    }

    @Test
    public void testWithCustomCompletions() {
        Parameter<String> parameter = Parameter.string();
        List<String> completions = Arrays.asList("eza", "123");

        assertThat(parameter.getCompletions()).isNotNull().isEmpty();

        assertThat(parameter.withCustomCompletions(completions.toArray(new String[0]))).isEqualTo(parameter);

        assertThat(parameter.isCustomCompletions()).isTrue();
        assertThat(parameter.getCompletions()).isNotNull()
                .hasSameSizeAs(completions)
                .hasSameElementsAs(completions);
    }

    @Test
    public void testOptional() {
        Parameter<Integer> parameter = Parameter.integer();
        assertThat(parameter.optional().isNeeded()).isFalse();
    }

    @Test
    public void testCheckValue() {
        Parameter<Integer> parameter = Parameter.integer();

        assertThat(parameter.checkValue("123")).isTrue();
        assertThat(parameter.checkValue("-18")).isTrue();
        assertThat(parameter.checkValue("1.23f")).isFalse();
        assertThat(parameter.checkValue("eza89")).isFalse();
        assertThat(parameter.checkValue("89g")).isFalse();
    }

    @Test
    public void testConvertValue() {
        Parameter<Integer> parameter = Parameter.integer();

        assertThat(parameter.convertValue("123")).isEqualTo(123);
        assertThat(parameter.convertValue("-18")).isEqualTo(-18);
        assertThatExceptionOfType(NumberFormatException.class)
                .isThrownBy(() -> parameter.convertValue("eza"))
                .withMessage("For input string: \"eza\"")
                .withNoCause();
    }

}

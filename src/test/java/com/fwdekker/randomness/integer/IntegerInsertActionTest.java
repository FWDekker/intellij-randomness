package com.fwdekker.randomness.integer;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;


/**
 * Parameterized unit tests for {@link IntegerInsertAction}.
 */
final class IntegerInsertActionTest {
    @SuppressWarnings("PMD.UnusedPrivateMethod") // Used as parameterized method source
    private static Collection<Object[]> provider() {
        return Arrays.asList(new Object[][]{
                {0, 0, "0"},
                {1, 1, "1"},
                {-5, -5, "-5"},
                {488, 488, "488"},
                {-876, -876, "-876"},
        });
    }


    @ParameterizedTest
    @MethodSource("provider")
    void testValue(final int minValue, final int maxValue, final String expectedString) {
        final IntegerSettings integerSettings = new IntegerSettings();
        integerSettings.setMinValue(minValue);
        integerSettings.setMaxValue(maxValue);

        final IntegerInsertAction insertRandomInteger = new IntegerInsertAction(integerSettings);
        final String randomString = insertRandomInteger.generateString();

        assertThat(randomString).isEqualTo(expectedString);
    }
}

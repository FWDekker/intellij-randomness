package com.fwdekker.randomness.integer;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;


/**
 * Unit tests for the base conversion used in {@link IntegerInsertAction}.
 */
final class IntegerInsertActionBaseTest {
    @SuppressWarnings("PMD.UnusedPrivateMethod") // Used as parameterized method source
    private static Collection<Object[]> provider() {
        return Arrays.asList(new Object[][]{
            {33360, 10, '.', "33.360"},
            {48345, 10, '.', "48.345"},
            {48345, 11, '.', "33360"},
        });
    }


    @ParameterizedTest
    @MethodSource("provider")
    void testValue(final long value, final int base, final char groupingSeparator, final String expectedString) {
        final IntegerSettings integerSettings = new IntegerSettings();
        integerSettings.setMinValue(value);
        integerSettings.setMaxValue(value);
        integerSettings.setBase(base);
        integerSettings.setGroupingSeparator(groupingSeparator);

        final IntegerInsertAction insertRandomInteger = new IntegerInsertAction(integerSettings);
        final String randomString = insertRandomInteger.generateString();

        assertThat(randomString).isEqualTo(expectedString);
    }
}

package com.fwdekker.randomness.integer;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;


/**
 * Unit tests for the symbols used in {@link IntegerInsertAction}.
 */
final class IntegerInsertActionSymbolTest {
    @SuppressWarnings("PMD.UnusedPrivateMethod") // Used as parameterized method source
    private static Collection<Object[]> provider() {
        return Arrays.asList(new Object[][]{
                {95713, '\0', "95713"},
                {163583, '.', "163.583"},
                {351426, ',', "351,426"},
        });
    }


    @ParameterizedTest
    @MethodSource("provider")
    void testValue(final long value, final char groupingSeparator, final String expectedString) {
        final IntegerSettings integerSettings = new IntegerSettings();
        integerSettings.setMinValue(value);
        integerSettings.setMaxValue(value);
        integerSettings.setGroupingSeparator(groupingSeparator);

        final IntegerInsertAction insertRandomInteger = new IntegerInsertAction(integerSettings);
        final String randomString = insertRandomInteger.generateString();

        assertThat(randomString).isEqualTo(expectedString);
    }
}

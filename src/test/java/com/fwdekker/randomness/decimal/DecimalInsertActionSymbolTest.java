package com.fwdekker.randomness.decimal;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;


/**
 * Unit tests for the symbols used in {@link DecimalInsertAction}.
 */
final class DecimalInsertActionSymbolTest {
    @SuppressWarnings("PMD.UnusedPrivateMethod") // Used as parameterized method source
    private static Collection<Object[]> provider() {
        return Arrays.asList(new Object[][]{
            {4.2, 2, '.', '.', "4.20"},
            {4.2, 2, '.', ',', "4,20"},
            {4.2, 2, ',', '.', "4.20"},
            {4.2, 2, ',', ',', "4,20"},

            {67575.845, 3, '\0', '.', "67575.845"},
            {67575.845, 3, '.', '.', "67.575.845"},
            {67575.845, 3, '.', ',', "67.575,845"},
            {67575.845, 3, ',', '.', "67,575.845"},
            {67575.845, 3, ',', ',', "67,575,845"},
        });
    }


    @ParameterizedTest
    @MethodSource("provider")
    void testValue(final double value, final int decimalCount, final char groupingSeparator,
                   final char decimalSeparator, final String expectedString) {
        final DecimalSettings decimalSettings = new DecimalSettings();
        decimalSettings.setMinValue(value);
        decimalSettings.setMaxValue(value);
        decimalSettings.setDecimalCount(decimalCount);
        decimalSettings.setGroupingSeparator(groupingSeparator);
        decimalSettings.setDecimalSeparator(decimalSeparator);

        final DecimalInsertAction insertRandomDecimal = new DecimalInsertAction(decimalSettings);
        final String randomString = insertRandomDecimal.generateString();

        assertThat(randomString).isEqualTo(expectedString);
    }
}

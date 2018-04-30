package com.fwdekker.randomness.decimal;

import java.util.Arrays;
import java.util.Collection;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import static org.assertj.core.api.Assertions.assertThat;


/**
 * Unit tests for the symbols used in {@link DecimalInsertAction}.
 */
@RunWith(Parameterized.class)
public final class DecimalInsertActionSymbolTest {
    private final double value;
    private final int decimalCount;
    private final char groupingSeparator;
    private final char decimalSeparator;
    private final String expectedString;


    public DecimalInsertActionSymbolTest(final double value, final int decimalCount, final char groupingSeparator,
                                         final char decimalSeparator, final String expectedString) {
        this.value = value;
        this.decimalCount = decimalCount;
        this.groupingSeparator = groupingSeparator;
        this.decimalSeparator = decimalSeparator;
        this.expectedString = expectedString;
    }


    @Parameterized.Parameters
    public static Collection<Object[]> params() {
        return Arrays.asList(new Object[][] {
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


    @Test
    public void testValue() {
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

package com.fwdekker.randomness.decimal;

import com.fwdekker.randomness.array.ArraySettings;
import java.util.Arrays;
import java.util.Collection;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import static org.assertj.core.api.Assertions.assertThat;


/**
 * Parameterized unit tests for {@link InsertRandomDecimal}.
 */
@RunWith(Parameterized.class)
public final class InsertRandomDecimalTest {
    private final double minValue;
    private final double maxValue;
    private final int decimalCount;
    private final String expectedString;


    public InsertRandomDecimalTest(final double minValue, final double maxValue, final int decimalCount,
                                   final String expectedString) {
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.decimalCount = decimalCount;
        this.expectedString = expectedString;
    }


    @Parameterized.Parameters
    public static Collection<Object[]> params() {
        return Arrays.asList(new Object[][] {
                {5.0, 5.0, 0, "5"},
                {7.0, 7.0, 1, "7.0"},
                {8.0, 8.0, 2, "8.00"},

                {47.6, 47.6, 0, "48"},
                {56.4, 56.4, 1, "56.4"},
                {73.7, 73.7, 2, "73.70"},

                {21.85, 21.85, 0, "22"},
                {79.59, 79.59, 1, "79.6"},
                {43.51, 43.51, 2, "43.51"},
                {83.82, 83.82, 3, "83.820"},

                {77.592, 77.592, 0, "78"},
                {14.772, 14.772, 1, "14.8"},
                {98.602, 98.602, 2, "98.60"},
                {32.675, 32.675, 3, "32.675"},
                {52.800, 52.800, 4, "52.8000"},

                {-22.252, -22.252, 0, "-22"},
                {-85.703, -85.703, 1, "-85.7"},
                {-52.686, -52.686, 2, "-52.69"},
                {-94.202, -94.202, 3, "-94.202"},
                {-60.152, -60.152, 4, "-60.1520"},
        });
    }


    @Test
    public void testValue() {
        final DecimalSettings decimalSettings = new DecimalSettings();
        decimalSettings.setMinValue(minValue);
        decimalSettings.setMaxValue(maxValue);
        decimalSettings.setDecimalCount(decimalCount);

        final InsertRandomDecimal insertRandomDecimal = new InsertRandomDecimal(new ArraySettings(), decimalSettings);
        final String randomString = insertRandomDecimal.generateString();

        assertThat(randomString).isEqualTo(expectedString);
    }
}

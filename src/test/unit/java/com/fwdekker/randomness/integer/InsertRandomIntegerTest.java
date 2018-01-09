package com.fwdekker.randomness.integer;

import java.util.Arrays;
import java.util.Collection;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import static org.assertj.core.api.Assertions.assertThat;


/**
 * Parameterized unit tests for {@link IntegerInsertAction}.
 */
@RunWith(Parameterized.class)
public final class InsertRandomIntegerTest {
    private final int minValue;
    private final int maxValue;
    private final String expectedString;


    public InsertRandomIntegerTest(final int minValue, final int maxValue, final String expectedString) {
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.expectedString = expectedString;
    }


    @Parameterized.Parameters
    public static Collection<Object[]> params() {
        return Arrays.asList(new Object[][] {
                {0, 0, "0"},
                {1, 1, "1"},
                {-5, -5, "-5"},
                {488, 488, "488"},
                {-876, -876, "-876"},
        });
    }


    @Test
    public void testValue() {
        final IntegerSettings integerSettings = new IntegerSettings();
        integerSettings.setMinValue(minValue);
        integerSettings.setMaxValue(maxValue);

        final IntegerInsertAction insertRandomInteger = new IntegerInsertAction(integerSettings);
        final String randomString = insertRandomInteger.generateString();

        assertThat(randomString).isEqualTo(expectedString);
    }
}

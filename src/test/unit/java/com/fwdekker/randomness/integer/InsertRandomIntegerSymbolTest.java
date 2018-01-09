package com.fwdekker.randomness.integer;

import java.util.Arrays;
import java.util.Collection;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import static org.assertj.core.api.Assertions.assertThat;


/**
 * Unit tests for the symbols used in {@link IntegerInsertAction}.
 */
@RunWith(Parameterized.class)
public final class InsertRandomIntegerSymbolTest {
    private final long value;
    private final char groupingSeparator;
    private final String expectedString;


    public InsertRandomIntegerSymbolTest(final long value, final char groupingSeparator, final String expectedString) {
        this.value = value;
        this.groupingSeparator = groupingSeparator;
        this.expectedString = expectedString;
    }


    @Parameterized.Parameters
    public static Collection<Object[]> params() {
        return Arrays.asList(new Object[][] {
                {95713, '\0', "95713"},
                {163583, '.', "163.583"},
                {351426, ',', "351,426"},
        });
    }


    @Test
    public void testValue() {
        final IntegerSettings integerSettings = new IntegerSettings();
        integerSettings.setMinValue(value);
        integerSettings.setMaxValue(value);
        integerSettings.setGroupingSeparator(groupingSeparator);

        final IntegerInsertAction insertRandomInteger = new IntegerInsertAction(integerSettings);
        final String randomString = insertRandomInteger.generateString();

        assertThat(randomString).isEqualTo(expectedString);
    }
}

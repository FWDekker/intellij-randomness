package com.fwdekker.randomness.integer;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;


/**
 * Unit tests for the base conversion used in {@link IntegerInsertAction}.
 */
@RunWith(Parameterized.class)
public final class IntegerInsertActionBaseTest {
    private final long value;
    private final int base;
    private final char groupingSeparator;
    private final String expectedString;


    public IntegerInsertActionBaseTest(final long value, final int base,
                                       final char groupingSeparator,
                                       final String expectedString) {
        this.value = value;
        this.base = base;
        this.groupingSeparator = groupingSeparator;
        this.expectedString = expectedString;
    }


    @Parameterized.Parameters
    public static Collection<Object[]> params() {
        return Arrays.asList(new Object[][]{
                {33360, 10, '.', "33.360"},
                {48345, 10, '.', "48.345"},
                {48345, 11, '.', "33360"},
        });
    }


    @Test
    public void testValue() {
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

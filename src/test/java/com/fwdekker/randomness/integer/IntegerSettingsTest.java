package com.fwdekker.randomness.integer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


/**
 * Unit tests for {@link IntegerSettings}.
 */
final class IntegerSettingsTest {
    private IntegerSettings integerSettings;


    @BeforeEach
    void beforeEach() {
        integerSettings = new IntegerSettings();
    }


    @Test
    void testGetLoadState() {
        integerSettings.setMinValue(742);
        integerSettings.setMaxValue(908);
        integerSettings.setBase(12);

        final IntegerSettings newIntegerSettings = new IntegerSettings();
        newIntegerSettings.loadState(integerSettings.getState());

        assertThat(newIntegerSettings.getMinValue()).isEqualTo(742);
        assertThat(newIntegerSettings.getMaxValue()).isEqualTo(908);
        assertThat(newIntegerSettings.getBase()).isEqualTo(12);
    }

    @Test
    void testGetSetMinValue() {
        integerSettings.setMinValue(366);

        assertThat(integerSettings.getMinValue()).isEqualTo(366);
    }

    @Test
    void testGetSetMaxValue() {
        integerSettings.setMaxValue(332);

        assertThat(integerSettings.getMaxValue()).isEqualTo(332);
    }

    @Test
    void testGetSetBase() {
        integerSettings.setBase(7);

        assertThat(integerSettings.getBase()).isEqualTo(7);
    }

    @Test
    void testGetSetGroupingSeparator() {
        integerSettings.setGroupingSeparator('6');

        assertThat(integerSettings.getGroupingSeparator()).isEqualTo('6');
    }

    @Test
    void testGetSetGroupingSeparatorStringEmpty() {
        integerSettings.setGroupingSeparator("");

        assertThat(integerSettings.getGroupingSeparator()).isEqualTo('\0');
    }

    @Test
    void testGetSetGroupingSeparatorString() {
        integerSettings.setGroupingSeparator("tlRg}");

        assertThat(integerSettings.getGroupingSeparator()).isEqualTo('t');
    }
}

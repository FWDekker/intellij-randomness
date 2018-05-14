package com.fwdekker.randomness.decimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


/**
 * Unit tests for {@link DecimalSettings}.
 */
final class DecimalSettingsTest {
    private DecimalSettings decimalSettings;


    @BeforeEach
    void beforeEach() {
        decimalSettings = new DecimalSettings();
    }


    @Test
    void testGetComponentName() {
        assertThat(decimalSettings.getComponentName()).isEqualTo("DecimalSettings");
    }

    @Test
    void testGetLoadState() {
        decimalSettings.setMinValue(399.75);
        decimalSettings.setMaxValue(928.22);
        decimalSettings.setDecimalCount(205);

        final DecimalSettings newDecimalSettings = new DecimalSettings();
        newDecimalSettings.loadState(decimalSettings.getState());

        assertThat(newDecimalSettings.getMinValue()).isEqualTo(399.75);
        assertThat(newDecimalSettings.getMaxValue()).isEqualTo(928.22);
        assertThat(newDecimalSettings.getDecimalCount()).isEqualTo(205);
    }

    @Test
    void testGetSetMinValue() {
        decimalSettings.setMinValue(720.41);

        assertThat(decimalSettings.getMinValue()).isEqualTo(720.41);
    }

    @Test
    void testGetSetMaxValue() {
        decimalSettings.setMaxValue(901.38);

        assertThat(decimalSettings.getMaxValue()).isEqualTo(901.38);
    }

    @Test
    void testGetSetDecimalCount() {
        decimalSettings.setDecimalCount(987);

        assertThat(decimalSettings.getDecimalCount()).isEqualTo(987);
    }

    @Test
    void testGetSetGroupingSeparator() {
        decimalSettings.setGroupingSeparator('L');

        assertThat(decimalSettings.getGroupingSeparator()).isEqualTo('L');
    }

    @Test
    void testGetSetGroupingSeparatorStringEmpty() {
        decimalSettings.setGroupingSeparator("");

        assertThat(decimalSettings.getGroupingSeparator()).isEqualTo('\0');
    }

    @Test
    void testGetSetGroupingSeparatorString() {
        decimalSettings.setGroupingSeparator("3lk-c");

        assertThat(decimalSettings.getGroupingSeparator()).isEqualTo('3');
    }

    @Test
    void testGetSetDecimalSeparator() {
        decimalSettings.setDecimalSeparator('}');

        assertThat(decimalSettings.getDecimalSeparator()).isEqualTo('}');
    }

    @Test
    void testGetSetDecimalSeparatorStringEmpty() {
        decimalSettings.setDecimalSeparator("");

        assertThat(decimalSettings.getDecimalSeparator()).isEqualTo('\0');
    }

    @Test
    void testGetSetDecimalSeparatorString() {
        decimalSettings.setDecimalSeparator("Px@>[");

        assertThat(decimalSettings.getDecimalSeparator()).isEqualTo('P');
    }
}

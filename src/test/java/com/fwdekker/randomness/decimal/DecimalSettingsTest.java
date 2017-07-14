package com.fwdekker.randomness.decimal;

import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;


/**
 * Unit tests for {@link DecimalSettings}.
 */
public final class DecimalSettingsTest {
    private DecimalSettings decimalSettings;


    @Before
    public void beforeEach() {
        decimalSettings = new DecimalSettings();
    }


    @Test
    public void testGetLoadState() {
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
    public void testGetSetMinValue() {
        decimalSettings.setMinValue(720.41);

        assertThat(decimalSettings.getMinValue()).isEqualTo(720.41);
    }

    @Test
    public void testGetSetMaxValue() {
        decimalSettings.setMaxValue(901.38);

        assertThat(decimalSettings.getMaxValue()).isEqualTo(901.38);
    }

    @Test
    public void testGetSetDecimalCount() {
        decimalSettings.setDecimalCount(987);

        assertThat(decimalSettings.getDecimalCount()).isEqualTo(987);
    }
}

package com.fwdekker.randomness.integer;

import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;


/**
 * Unit tests for {@link IntegerSettings}.
 */
public final class IntegerSettingsTest {
    private IntegerSettings integerSettings;


    @Before
    public void beforeEach() {
        integerSettings = new IntegerSettings();
    }


    @Test
    public void testGetComponentName() {
        assertThat(integerSettings.getComponentName()).isEqualTo("IntegerSettings");
    }

    @Test
    public void testGetLoadState() {
        integerSettings.setMinValue(742);
        integerSettings.setMaxValue(908);

        final IntegerSettings newIntegerSettings = new IntegerSettings();
        newIntegerSettings.loadState(integerSettings.getState());

        assertThat(newIntegerSettings.getMinValue()).isEqualTo(742);
        assertThat(newIntegerSettings.getMaxValue()).isEqualTo(908);
    }

    @Test
    public void testGetSetMinValue() {
        integerSettings.setMinValue(366);

        assertThat(integerSettings.getMinValue()).isEqualTo(366);
    }

    @Test
    public void testGetSetMaxValue() {
        integerSettings.setMaxValue(332);

        assertThat(integerSettings.getMaxValue()).isEqualTo(332);
    }
}

package com.fwdekker.randomness.ui;

import com.fwdekker.randomness.ValidationException;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


/**
 * Unit tests for {@link JDoubleSpinner}.
 */
public final class JDoubleSpinnerTest {
    @Test
    public void testIllegalMinValue() {
        assertThatThrownBy(() -> new JDoubleSpinner(-1E80, -477.23))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("minValue should not be smaller than -1.0E53.");
    }

    @Test
    public void testIllegalMaxValue() {
        assertThatThrownBy(() -> new JDoubleSpinner(-161.29, 1E73))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("maxValue should not be greater than 1.0E53.");
    }

    @Test
    public void testIllegalRange() {
        assertThatThrownBy(() -> new JDoubleSpinner(-602.98, -929.41))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("minValue should be greater than maxValue.");
    }


    @Test
    public void testGetSetValue() {
        final JDoubleSpinner spinner = new JDoubleSpinner();

        spinner.setValue(179.40);

        assertThat(spinner.getValue()).isEqualTo(179.40);
    }

    @Test
    public void testGetSetValueType() {
        final JDoubleSpinner spinner = new JDoubleSpinner();

        spinner.setValue(638L);

        assertThat(spinner.getValue()).isEqualTo(638.0);
    }


    @Test
    public void testValidateUnderflow() {
        final JDoubleSpinner spinner = new JDoubleSpinner();

        spinner.setValue(-1E55);

        assertThatThrownBy(() -> spinner.validateValue())
                .isInstanceOf(ValidationException.class)
                .hasMessage("Please enter a value greater than or equal to -1.0E53.");
    }

    @Test
    public void testValidateOverflow() {
        final JDoubleSpinner spinner = new JDoubleSpinner();

        spinner.setValue(1E98);

        assertThatThrownBy(() -> spinner.validateValue())
                .isInstanceOf(ValidationException.class)
                .hasMessage("Please enter a value less than or equal to 1.0E53.");
    }

    @Test
    public void testValidateUnderflowCustomRange() {
        final JDoubleSpinner spinner = new JDoubleSpinner(-738.33, 719.45);

        spinner.setValue(-808.68);

        assertThatThrownBy(() -> spinner.validateValue())
                .isInstanceOf(ValidationException.class)
                .hasMessage("Please enter a value greater than or equal to -738.33.");
    }

    @Test
    public void testValidateOverflowCustomRange() {
        final JDoubleSpinner spinner = new JDoubleSpinner(-972.80, -69.36);

        spinner.setValue(94.0);

        assertThatThrownBy(() -> spinner.validateValue())
                .isInstanceOf(ValidationException.class)
                .hasMessage("Please enter a value less than or equal to -69.36.");
    }
}

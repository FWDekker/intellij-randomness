package com.fwdekker.randomness.ui;

import com.fwdekker.randomness.ValidationException;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


/**
 * Unit tests for {@link JLongSpinner}.
 */
public final class JLongSpinnerTest {
    @Test
    public void testIllegalRange() {
        assertThatThrownBy(() -> new JLongSpinner(989, -339))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("minValue should be greater than maxValue.");
    }


    @Test
    public void testGetSetValue() {
        final JLongSpinner spinner = new JLongSpinner();

        spinner.setValue(-583L);

        assertThat(spinner.getValue()).isEqualTo(-583L);
    }

    @Test
    public void testGetSetValueType() {
        final JLongSpinner spinner = new JLongSpinner();

        spinner.setValue(125);

        assertThat(spinner.getValue()).isEqualTo(125L);
    }

    @Test
    public void testGetSetValueTruncation() {
        final JLongSpinner spinner = new JLongSpinner();

        spinner.setValue(786.79);

        assertThat(spinner.getValue()).isEqualTo(786L);
    }


    @Test
    public void testGetSetMinValue() {
        final JLongSpinner spinner = new JLongSpinner();

        spinner.setMinValue(979L);

        assertThat(spinner.getMinValue()).isEqualTo(979L);
    }

    @Test
    public void testGetSetMaxValue() {
        final JLongSpinner spinner = new JLongSpinner();

        spinner.setMinValue(166L);

        assertThat(spinner.getMinValue()).isEqualTo(166L);
    }


    @Test
    public void testValidateUnderflowCustomRange() {
        final JLongSpinner spinner = new JLongSpinner(-950, -559);

        spinner.setValue(-979);

        assertThatThrownBy(() -> spinner.validateValue())
                .isInstanceOf(ValidationException.class)
                .hasMessage("Please enter a value greater than or equal to -950.");
    }

    @Test
    public void testValidateOverflowCustomRange() {
        final JLongSpinner spinner = new JLongSpinner(279, 678);

        spinner.setValue(838);

        assertThatThrownBy(() -> spinner.validateValue())
                .isInstanceOf(ValidationException.class)
                .hasMessage("Please enter a value less than or equal to 678.");
    }
}

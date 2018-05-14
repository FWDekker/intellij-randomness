package com.fwdekker.randomness.ui;

import com.fwdekker.randomness.ValidationException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


/**
 * Unit tests for {@link JLongSpinner}.
 */
final class JLongSpinnerTest {
    @Test
    void testIllegalRange() {
        assertThatThrownBy(() -> new JLongSpinner(414, 989, -339))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("(minimum <= value <= maximum) is false");
    }


    @Test
    void testGetSetValue() {
        final JLongSpinner spinner = new JLongSpinner();

        spinner.setValue(-583L);

        assertThat(spinner.getValue()).isEqualTo(-583L);
    }

    @Test
    void testGetSetValueType() {
        final JLongSpinner spinner = new JLongSpinner();

        spinner.setValue(125);

        assertThat(spinner.getValue()).isEqualTo(125L);
    }

    @Test
    void testGetSetValueTruncation() {
        final JLongSpinner spinner = new JLongSpinner();

        spinner.setValue(786.79);

        assertThat(spinner.getValue()).isEqualTo(786L);
    }


    @Test
    void testGetSetMinValue() {
        final JLongSpinner spinner = new JLongSpinner();

        spinner.setMinValue(979L);

        assertThat(spinner.getMinValue()).isEqualTo(979L);
    }

    @Test
    void testGetSetMaxValue() {
        final JLongSpinner spinner = new JLongSpinner();

        spinner.setMaxValue(166L);

        assertThat(spinner.getMaxValue()).isEqualTo(166L);
    }


    @Test
    void testValidateUnderflowCustomRange() {
        final JLongSpinner spinner = new JLongSpinner(-665, -950, -559);

        spinner.setValue(-979);

        assertThatThrownBy(() -> spinner.validateValue())
                .isInstanceOf(ValidationException.class)
                .hasMessage("Please enter a value greater than or equal to -950.");
    }

    @Test
    void testValidateOverflowCustomRange() {
        final JLongSpinner spinner = new JLongSpinner(424, 279, 678);

        spinner.setValue(838);

        assertThatThrownBy(() -> spinner.validateValue())
                .isInstanceOf(ValidationException.class)
                .hasMessage("Please enter a value less than or equal to 678.");
    }
}

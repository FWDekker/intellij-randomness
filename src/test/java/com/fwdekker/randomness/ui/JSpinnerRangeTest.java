package com.fwdekker.randomness.ui;

import com.fwdekker.randomness.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.swing.JSpinner;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


/**
 * Unit tests for {@link JSpinnerRange}.
 */
final class JSpinnerRangeTest {
    private JSpinner min;
    private JSpinner max;


    @BeforeEach
    void beforeEach() {
        min = mock(JSpinner.class);
        max = mock(JSpinner.class);
    }


    @Test
    void testIllegalMaxRange() {
        assertThatThrownBy(() -> new JSpinnerRange(min, max, -37.20))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("maxRange must be a positive number.");
    }


    @Test
    void testRangeRelative() {
        when(min.getValue()).thenReturn(85.20);
        when(max.getValue()).thenReturn(-636.33);

        final JSpinnerRange range = new JSpinnerRange(min, max);

        assertThatThrownBy(() -> range.validate())
                .isInstanceOf(ValidationException.class)
                .hasMessage("The maximum should be no smaller than the minimum.");
    }

    @Test
    void testRangeSize() {
        when(min.getValue()).thenReturn(-1E53);
        when(max.getValue()).thenReturn(1E53);

        final JSpinnerRange range = new JSpinnerRange(min, max);

        assertThatThrownBy(() -> range.validate())
                .isInstanceOf(ValidationException.class)
                .hasMessage("The range should not exceed 1.0E53.");
    }

    @Test
    void testRangeSizeCustomRange() {
        when(min.getValue()).thenReturn(-794.90);
        when(max.getValue()).thenReturn(769.52);

        final JSpinnerRange range = new JSpinnerRange(min, max, 793.31);

        assertThatThrownBy(() -> range.validate())
                .isInstanceOf(ValidationException.class)
                .hasMessage("The range should not exceed 793.31.");
    }
}

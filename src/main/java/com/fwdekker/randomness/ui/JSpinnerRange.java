package com.fwdekker.randomness.ui;

import com.fwdekker.randomness.ValidationException;
import javax.swing.JSpinner;


/**
 * A container for two {@link JSpinner}s that indicate a range of values.
 */
public final class JSpinnerRange {
    /**
     * The maximum span that can be expressed by a {@code JSpinnerRange}.
     */
    private static final double DEFAULT_MAX_RANGE = 1E53;

    /**
     * The {@code JSpinner} that contains the minimum value.
     */
    private final JSpinner min;
    /**
     * The {@code JSpinner} that contains the maximum value.
     */
    private final JSpinner max;
    /**
     * The maximum span that can may expressed by this {@code JSpinnerRange}.
     */
    private final double maxRange;


    /**
     * Constructs a new {@code JSpinnerRange}.
     *
     * @param min the {@code JSpinner} that contains the minimum value
     * @param max the {@code JSpinner} that contains the maximum value
     */
    public JSpinnerRange(final JSpinner min, final JSpinner max) {
        this.min = min;
        this.max = max;

        this.maxRange = DEFAULT_MAX_RANGE;
    }

    /**
     * Constructs a new {@code JSpinnerRange}.
     *
     * @param min      the {@code JSpinner} that contains the minimum value
     * @param max      the {@code JSpinner} that contains the maximum value
     * @param maxRange the maximum span that can may expressed by this {@code JSpinnerRange}
     */
    public JSpinnerRange(final JSpinner min, final JSpinner max, final double maxRange) {
        if (maxRange < 0) {
            throw new IllegalArgumentException("maxRange must be a positive number.");
        }

        this.min = min;
        this.max = max;
        this.maxRange = maxRange;
    }


    /**
     * Validates this range.
     *
     * @throws ValidationException if the {@code JSpinner}s do not form a valid range
     */
    public void validate() throws ValidationException {
        final double minValue = ((Number) (min.getValue())).doubleValue();
        final double maxValue = ((Number) (max.getValue())).doubleValue();

        if (minValue > maxValue) {
            throw new ValidationException("The maximum should be no smaller than the minimum.", max);
        }
        if (maxValue - minValue > maxRange) {
            throw new ValidationException("The range should not exceed " + maxRange + ".", max);
        }
    }
}

package com.fwdekker.randomness.ui;

import com.fwdekker.randomness.ValidationException;

import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import java.text.DecimalFormatSymbols;
import java.util.Locale;


/**
 * A {@link JSpinner} for doubles.
 * <p>
 * A {@code JDoubleSpinner} can only represent value from {@value DEFAULT_MIN_VALUE} (inclusive) until {@value
 * DEFAULT_MAX_VALUE} (inclusive), because not all numbers outside this range can be represented as a {@link Double}.
 */
public final class JDoubleSpinner extends JSpinner {
    /**
     * The default step size when decrementing or incrementing the value.
     */
    private static final double DEFAULT_STEP_SIZE = 0.1;
    /**
     * The smallest number that can be represented by a {@code JDoubleSpinner}.
     */
    private static final double DEFAULT_MIN_VALUE = -1E53;
    /**
     * The largest number that may be represented by a {@code JDoubleSpinner}.
     */
    private static final double DEFAULT_MAX_VALUE = 1E53;

    /**
     * The smallest number that may be represented by this {@code JDoubleSpinner}.
     */
    private final double minValue;
    /**
     * The largest number that may be represented by this {@code JDoubleSpinner}.
     */
    private final double maxValue;


    /**
     * Constructs a new {@code JDoubleSpinner}.
     */
    public JDoubleSpinner() {
        super(new SpinnerNumberModel(0.0, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, DEFAULT_STEP_SIZE));

        this.minValue = DEFAULT_MIN_VALUE;
        this.maxValue = DEFAULT_MAX_VALUE;

        final JSpinner.NumberEditor editor = new JSpinner.NumberEditor(this);
        editor.getFormat().setDecimalFormatSymbols(new DecimalFormatSymbols(Locale.US));
        setEditor(editor);
    }

    /**
     * Constructs a new {@code JDoubleSpinner}.
     *
     * @param minValue the smallest number that may be represented by this {@code JDoubleSpinner}
     * @param maxValue the largest number that may be represented by this {@code JDoubleSpinner}
     */
    public JDoubleSpinner(final double minValue, final double maxValue) {
        if (minValue < DEFAULT_MIN_VALUE) {
            throw new IllegalArgumentException("minValue should not be smaller than " + DEFAULT_MIN_VALUE + ".");
        }
        if (maxValue > DEFAULT_MAX_VALUE) {
            throw new IllegalArgumentException("maxValue should not be greater than " + DEFAULT_MAX_VALUE + ".");
        }
        if (minValue > maxValue) {
            throw new IllegalArgumentException("minValue should be greater than maxValue.");
        }

        this.minValue = minValue;
        this.maxValue = maxValue;
    }


    @Override
    public Double getValue() {
        return ((Number) super.getValue()).doubleValue();
    }

    /**
     * Validates the current value and throws an exception if the value is invalid.
     *
     * @throws ValidationException if the value is not valid
     */
    public void validateValue() throws ValidationException {
        final double value = getValue();

        if (value < minValue) {
            throw new ValidationException("Please enter a value greater than or equal to " + minValue + ".", this);
        }
        if (value > maxValue) {
            throw new ValidationException("Please enter a value less than or equal to " + maxValue + ".", this);
        }
    }
}

package com.fwdekker.randomness.ui;

import com.fwdekker.randomness.ValidationException;

import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import java.awt.Dimension;
import java.text.DecimalFormatSymbols;
import java.util.Locale;


/**
 * A {@link JSpinner} for longs.
 * <p>
 * A {@code JLongSpinner} can only represent value from {@value DEFAULT_MIN_VALUE} (inclusive) until {@value
 * DEFAULT_MAX_VALUE} (inclusive), because not all numbers outside this range can be represented as a {@link Double}.
 */
public final class JLongSpinner extends JSpinner {
    /**
     * The default value represented by a {@code JDoubleSpinner}.
     */
    private static final long DEFAULT_VALUE = 0L;
    /**
     * The smallest number that can be represented by a {@code JDoubleSpinner}.
     */
    private static final long DEFAULT_MIN_VALUE = Long.MIN_VALUE;
    /**
     * The largest number that can be represented by a {@code JDoubleSpinner}.
     */
    private static final long DEFAULT_MAX_VALUE = Long.MAX_VALUE;
    /**
     * The default step size when decrementing or incrementing the value.
     */
    private static final long DEFAULT_STEP_SIZE = 1L;
    /**
     * The minimal and preferred width of this component.
     */
    private static final int SPINNER_WIDTH = 52;

    /**
     * The smallest number that may be represented by this {@code JDoubleSpinner}.
     */
    private long minValue;
    /**
     * The largest number that may be represented by this {@code JDoubleSpinner}.
     */
    private long maxValue;


    /**
     * Constructs a new {@code JLongSpinner}.
     */
    public JLongSpinner() {
        this(DEFAULT_VALUE, DEFAULT_MIN_VALUE, DEFAULT_MAX_VALUE);
    }

    /**
     * Constructs a new {@code JLongSpinner}.
     *
     * @param value    the default value represented by this {@code JDoubleSpinner}
     * @param minValue the smallest number that may be represented by this {@code JDoubleSpinner}
     * @param maxValue the largest number that may be represented by this {@code JDoubleSpinner}
     */
    public JLongSpinner(final long value, final long minValue, final long maxValue) {
        super(new SpinnerNumberModel((Long) value, (Long) minValue, (Long) maxValue, (Long) DEFAULT_STEP_SIZE));

        this.minValue = minValue;
        this.maxValue = maxValue;

        final JSpinner.NumberEditor editor = new JSpinner.NumberEditor(this);
        editor.getFormat().setDecimalFormatSymbols(new DecimalFormatSymbols(Locale.US));
        setEditor(editor);

        final Dimension minimumSize = getMinimumSize();
        minimumSize.width = SPINNER_WIDTH;
        setMinimumSize(minimumSize);

        final Dimension preferredSize = getPreferredSize();
        preferredSize.width = SPINNER_WIDTH;
        setPreferredSize(preferredSize);
    }


    @Override
    public Long getValue() {
        return ((Number) super.getValue()).longValue();
    }

    /**
     * Validates the current value and throws an exception if the value is invalid.
     *
     * @throws ValidationException if the value is not valid
     */
    public void validateValue() throws ValidationException {
        final long value = getValue();

        if (value < minValue) {
            throw new ValidationException("Please enter a value greater than or equal to " + minValue + ".", this);
        }
        if (value > maxValue) {
            throw new ValidationException("Please enter a value less than or equal to " + maxValue + ".", this);
        }
    }


    /**
     * Returns the smallest number that may be represented by this {@code JDoubleSpinner}.
     *
     * @return the smallest number that may be represented by this {@code JDoubleSpinner}
     */
    public long getMinValue() {
        return minValue;
    }

    /**
     * Sets the smallest number that may be represented by this {@code JDoubleSpinner}.
     *
     * @param minValue the smallest number that may be represented by this {@code JDoubleSpinner}
     */
    public void setMinValue(final long minValue) {
        this.minValue = minValue;
    }

    /**
     * Returns the largest number that may be represented by this {@code JDoubleSpinner}.
     *
     * @return the largest number that may be represented by this {@code JDoubleSpinner}
     */
    public long getMaxValue() {
        return maxValue;
    }

    /**
     * Sets the largest number that may be represented by this {@code JDoubleSpinner}.
     *
     * @param maxValue the largest number that may be represented by this {@code JDoubleSpinner}
     */
    public void setMaxValue(final long maxValue) {
        this.maxValue = maxValue;
    }
}

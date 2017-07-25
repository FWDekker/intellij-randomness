package com.fwdekker.randomness.common;

import javax.swing.JList;
import javax.swing.JSpinner;


/**
 * Helper class for validating {@link javax.swing.JComponent}s.
 * <p>
 * A validation method does nothing if the component is valid, and throws a {@link ValidationException} if the component
 * is invalid.
 */
public final class Validator {
    /**
     * Private constructor to prevent instantiation.
     */
    private Validator() {
    }


    /**
     * Throws a {@code ValidationException} if the difference between the values of {@code min} and {@code max} is
     * infinity or if it is greater than {@code size}.
     * <p>
     * The thrown exception's component is {@code max}.
     *
     * @param min  the spinner that forms the start of the range
     * @param max  the spinner that forms the end of the range
     * @param size the maximum difference between the spinners' values
     * @throws ValidationException if {@code min}'s value is greater than that of {@code max}
     */
    public static void areValidRange(final JSpinner min, final JSpinner max, final double size)
            throws ValidationException {
        final double minValue = getSpinnerValue(min);
        final double maxValue = getSpinnerValue(max);

        if (minValue > maxValue) {
            throw new ValidationException("The maximum should be no smaller than the minimum.", max);
        }
        if (maxValue - minValue == Double.POSITIVE_INFINITY) {
            throw new ValidationException("The range should not exceed " + size + ".", max);
        }
        if (maxValue - minValue > size) {
            throw new ValidationException("The range should not exceed " + size + ".", max);
        }
    }

    /**
     * Throws a {@code ValidationException} if no values are selected in the list.
     *
     * @param list a list
     * @throws ValidationException if no values are selected in the list
     */
    public static void isNotEmpty(final JList list) throws ValidationException {
        if (list.getSelectedValuesList().isEmpty()) {
            throw new ValidationException("Please select at least one option.", list);
        }
    }


    /**
     * Returns the value in the given spinner as a double.
     *
     * @param spinner a spinner
     * @return the value in the given spinner as a double
     */
    private static double getSpinnerValue(final JSpinner spinner) {
        return ((Number) spinner.getValue()).doubleValue();
    }
}

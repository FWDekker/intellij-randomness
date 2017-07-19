package com.fwdekker.randomness.common;

import java.text.ParseException;
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
     * Throws a {@code ValidationException} if calling {@link JSpinner#commitEdit()} results in a {@link
     * ParseException}.
     *
     * @param spinner a spinner
     * @throws ValidationException if calling {@link JSpinner#commitEdit()} results in a {@link ParseException}
     */
    public static void hasValidFormat(final JSpinner spinner) throws ValidationException {
        try {
            spinner.commitEdit();
        } catch (final ParseException e) {
            throw new ValidationException("Please enter a number.", e, spinner);
        }
    }

    /**
     * Throws a {@code ValidationException} if {@code max}'s value is less than that of {@code min}.
     * <p>
     * The thrown exception's component is {@code max}.
     *
     * @param min the spinner that forms the start of the range
     * @param max the spinner that forms the end of the range
     * @throws ValidationException if {@code min}'s value is greater than that of {@code max}
     */
    public static void areValidRange(final JSpinner min, final JSpinner max) throws ValidationException {
        final double minValue = getSpinnerValue(min);
        final double maxValue = getSpinnerValue(max);

        if (minValue > maxValue) {
            throw new ValidationException("The maximum should be no smaller than the minimum.", max);
        }
    }

    /**
     * Throws a {@code ValidationException} if the spinner's value is less than or equal to the given value.
     *
     * @param spinner a spinner
     * @param value   the value the spinner should be greater than
     * @throws ValidationException if the spinner's value is less than or equal to the given value
     */
    public static void isGreaterThan(final JSpinner spinner, final double value) throws ValidationException {
        if (getSpinnerValue(spinner) <= value) {
            throw new ValidationException("Please enter a value greater than " + value + ".", spinner);
        }
    }

    /**
     * Throws a {@code ValidationException} if the spinner's value is greater than or equal to the given value.
     *
     * @param spinner a spinner
     * @param value   the value the spinner should be less than
     * @throws ValidationException if the spinner's value is greater than or equal to the given value
     */
    public static void isLessThan(final JSpinner spinner, final double value) throws ValidationException {
        if (getSpinnerValue(spinner) >= value) {
            throw new ValidationException("Please enter a value less than " + value + ".", spinner);
        }
    }

    /**
     * Throws a {@code ValidationException} if the spinner's value is not a whole number.
     *
     * @param spinner a spinner
     * @throws ValidationException if the spinner's value is not a whole number
     */
    public static void isInteger(final JSpinner spinner) throws ValidationException {
        if (getSpinnerValue(spinner) % 1 != 0) {
            throw new ValidationException("Please enter a whole number.", spinner);
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

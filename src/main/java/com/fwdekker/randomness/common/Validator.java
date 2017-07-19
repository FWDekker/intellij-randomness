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
     * @param message the message of the thrown {@code ValidationException}
     * @throws ValidationException if calling {@link JSpinner#commitEdit()} results in a {@link ParseException}
     */
    public static void hasValidFormat(final JSpinner spinner, final String message) throws ValidationException {
        try {
            spinner.commitEdit();
        } catch (final ParseException e) {
            throw new ValidationException(message, e, spinner);
        }
    }

    /**
     * Throws a {@code ValidationException} if {@code max}'s value is less than that of {@code min}.
     * <p>
     * The thrown exception's component is {@code max}.
     *
     * @param min     the spinner that forms the start of the range
     * @param max     the spinner that forms the end of the range
     * @param message the message of the thrown {@code ValidationException}
     * @throws ValidationException if {@code min}'s value is greater than that of {@code max}
     */
    public static void areValidRange(final JSpinner min, final JSpinner max, final String message)
            throws ValidationException {
        final double minValue = getSpinnerValue(min);
        final double maxValue = getSpinnerValue(max);

        if (minValue > maxValue) {
            throw new ValidationException(message, max);
        }
    }

    /**
     * Throws a {@code ValidationException} if the spinner's value is less than or equal to the given value.
     *
     * @param spinner a spinner
     * @param value   the value the spinner should be greater than
     * @param message the message of the thrown {@code ValidationException}
     * @throws ValidationException if the spinner's value is less than or equal to the given value
     */
    public static void isGreaterThan(final JSpinner spinner, final double value, final String message)
            throws ValidationException {
        if (getSpinnerValue(spinner) <= value) {
            throw new ValidationException(message, spinner);
        }
    }

    /**
     * Throws a {@code ValidationException} if the spinner's value is greater than or equal to the given value.
     *
     * @param spinner a spinner
     * @param value   the value the spinner should be less than
     * @param message the message of the thrown {@code ValidationException}
     * @throws ValidationException if the spinner's value is greater than or equal to the given value
     */
    public static void isLessThan(final JSpinner spinner, final double value, final String message)
            throws ValidationException {
        if (getSpinnerValue(spinner) >= value) {
            throw new ValidationException(message, spinner);
        }
    }

    /**
     * Throws a {@code ValidationException} if the spinner's value is not a whole number.
     *
     * @param spinner a spinner
     * @param message the message of the thrown {@code ValidationException}
     * @throws ValidationException if the spinner's value is not a whole number
     */
    public static void isInteger(final JSpinner spinner, final String message) throws ValidationException {
        if (getSpinnerValue(spinner) % 1 != 0) {
            throw new ValidationException(message, spinner);
        }
    }

    /**
     * Throws a {@code ValidationException} if no values are selected in the list.
     *
     * @param list    a list
     * @param message the message of the thrown {@code ValidationException}
     * @throws ValidationException if no values are selected in the list
     */
    public static void isNotEmpty(final JList list, final String message) throws ValidationException {
        if (list.getSelectedValuesList().isEmpty()) {
            throw new ValidationException(message, list);
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

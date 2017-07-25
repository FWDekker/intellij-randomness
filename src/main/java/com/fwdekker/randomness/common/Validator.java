package com.fwdekker.randomness.common;

import javax.swing.JList;


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
}

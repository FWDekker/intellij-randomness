package com.fwdekker.randomness.common;

import javax.swing.JComponent;
import org.jetbrains.annotations.NotNull;


/**
 * An exception indicating that the validation of some {@code JComponent} has failed.
 */
public final class ValidationException extends Exception {
    private final JComponent component;


    /**
     * Constructs a new {@code ValidationException}.
     *
     * @param message   the detail message
     * @param component the component
     * @see Exception#Exception(String)
     */
    public ValidationException(final String message, @NotNull final JComponent component) {
        super(message);

        this.component = component;
    }


    /**
     * Returns the invalid component.
     *
     * @return the invalid component
     */
    public JComponent getComponent() {
        return component;
    }
}

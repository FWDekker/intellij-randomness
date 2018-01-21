package com.fwdekker.randomness;

import org.jetbrains.annotations.NotNull;

import javax.swing.JComponent;


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
    public ValidationException(final String message, final @NotNull JComponent component) {
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

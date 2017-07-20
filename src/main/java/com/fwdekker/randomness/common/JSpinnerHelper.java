package com.fwdekker.randomness.common;

import java.text.DecimalFormatSymbols;
import java.util.Locale;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;


/**
 * Helper class for working with {@link JSpinner}s.
 * <p>
 * In particular, it provides methods for creating {@code JSpinner} instances using the US locale to enforce the decimal
 * separator.
 */
public final class JSpinnerHelper {
    /**
     * Private constructor to prevent instantiation.
     */
    private JSpinnerHelper() {
    }


    /**
     * Creates a new {@code JSpinner} for entering decimals.
     *
     * @return a new {@code JSpinner} for entering decimals
     */
    public static JSpinner createSpinner() {
        final SpinnerNumberModel model
                = new SpinnerNumberModel(0.0, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, 0.1);
        final JSpinner spinner = new JSpinner(model);

        final JSpinner.NumberEditor editor = new JSpinner.NumberEditor(spinner);
        editor.getFormat().setDecimalFormatSymbols(new DecimalFormatSymbols(Locale.US));
        spinner.setEditor(editor);

        return spinner;
    }
}

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
     * Creates a new {@code JSpinner} for entering longs.
     *
     * @return a new {@code JSpinner} for entering longs
     */
    public static JSpinner createLongSpinner() {
        final SpinnerNumberModel model = new SpinnerNumberModel(0L, Long.MIN_VALUE, Long.MAX_VALUE, 1L);

        return createSpinner(model);
    }

    /**
     * Creates a new {@code JSpinner} for entering decimals.
     *
     * @return a new {@code JSpinner} for entering decimals
     */
    public static JSpinner createDecimalSpinner() {
        final SpinnerNumberModel model = new SpinnerNumberModel(0.0, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, 0.1);

        return createSpinner(model);
    }


    /**
     * Creates a new {@code JSpinner} from the given model.
     *
     * @param model a model
     * @return a new {@code JSpinner} from the given model
     */
    private static JSpinner createSpinner(final SpinnerNumberModel model) {
        final JSpinner spinner = new JSpinner(model);

        final JSpinner.NumberEditor editor = new JSpinner.NumberEditor(spinner);
        editor.getFormat().setDecimalFormatSymbols(new DecimalFormatSymbols(Locale.US));
        spinner.setEditor(editor);

        return spinner;
    }
}

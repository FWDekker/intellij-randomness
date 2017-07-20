package com.fwdekker.randomness.common;

import java.awt.Dimension;
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
    private static final int SPINNER_WIDTH = 52;


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
    public static JSpinner createDecimalSpinner() {
        final SpinnerNumberModel model
                = new SpinnerNumberModel(0.0, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, 0.1);
        final JSpinner spinner = new JSpinner(model);

        final JSpinner.NumberEditor editor = new JSpinner.NumberEditor(spinner);
        editor.getFormat().setDecimalFormatSymbols(new DecimalFormatSymbols(Locale.US));
        spinner.setEditor(editor);

        return spinner;
    }

    /**
     * Creates a new {@code JSpinner} for entering longs.
     *
     * @return a new {@code JSpinner} for entering longs
     */
    public static JSpinner createLongSpinner() {
        final SpinnerNumberModel model
                = new SpinnerNumberModel((Long) 0L, (Long) Long.MIN_VALUE, (Long) Long.MAX_VALUE, (Long) 1L);
        final JSpinner spinner = new JSpinner(model);

        final JSpinner.NumberEditor editor = new JSpinner.NumberEditor(spinner);
        editor.getFormat().setDecimalFormatSymbols(new DecimalFormatSymbols(Locale.US));
        spinner.setEditor(editor);

        final Dimension minimumSize = spinner.getMinimumSize();
        minimumSize.width = SPINNER_WIDTH;
        spinner.setMinimumSize(minimumSize);
        final Dimension preferredSize = spinner.getPreferredSize();
        preferredSize.width = SPINNER_WIDTH;
        spinner.setPreferredSize(preferredSize);

        return spinner;
    }
}

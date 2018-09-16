package com.fwdekker.randomness.decimal;

import com.fwdekker.randomness.DataInsertAction;
import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.concurrent.ThreadLocalRandom;


/**
 * Generates a random integer based on the settings in {@link DecimalSettings}.
 */
public final class DecimalInsertAction extends DataInsertAction {
    private final DecimalSettings decimalSettings;


    /**
     * Constructs a new {@code DecimalInsertAction} that uses the singleton {@code DecimalSettings} instance.
     */
    public DecimalInsertAction() {
        this.decimalSettings = DecimalSettings.getInstance();
    }

    /**
     * Constructs a new {@code DecimalInsertAction} that uses the given {@code DecimalSettings} instance.
     *
     * @param decimalSettings the settings to use for generating decimals
     */
    DecimalInsertAction(final @NotNull DecimalSettings decimalSettings) {
        this.decimalSettings = decimalSettings;
    }


    @Override
    protected String getName() {
        return "Insert Decimal";
    }

    /**
     * Returns a random integer between the minimum and maximum value, inclusive.
     *
     * @return a random integer between the minimum and maximum value, inclusive
     */
    @Override
    public String generateString() {
        final double randomValue = ThreadLocalRandom.current()
                .nextDouble(decimalSettings.getMinValue(), Math.nextUp(decimalSettings.getMaxValue()));

        return convertToString(randomValue);
    }


    /**
     * Returns a nicely formatted representation of a double.
     *
     * @param decimal a {@code double}
     * @return a nicely formatted representation of a double
     */
    private String convertToString(final double decimal) {
        final DecimalFormat format = new DecimalFormat();
        format.setGroupingUsed(decimalSettings.getGroupingSeparator() != '\0');

        final DecimalFormatSymbols symbols = format.getDecimalFormatSymbols();
        symbols.setGroupingSeparator(decimalSettings.getGroupingSeparator());
        symbols.setDecimalSeparator(decimalSettings.getDecimalSeparator());
        format.setMinimumFractionDigits(decimalSettings.getDecimalCount());
        format.setMaximumFractionDigits(decimalSettings.getDecimalCount());
        format.setDecimalFormatSymbols(symbols);

        return format.format(decimal);
    }


    /**
     * Inserts an array of decimals.
     */
    public final class ArrayAction extends DataInsertAction.ArrayAction {
        /**
         * Constructs a new {@code ArrayAction} for decimals.
         */
        public ArrayAction() {
            super(DecimalInsertAction.this);
        }


        @Override
        protected String getName() {
            return "Insert Decimal Array";
        }
    }
}

package com.fwdekker.randomness.integer;

import com.fwdekker.randomness.DataArrayInsertAction;
import com.fwdekker.randomness.DataInsertAction;
import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.concurrent.ThreadLocalRandom;


/**
 * Generates a random integer based on the settings in {@link IntegerSettings}.
 */
public final class IntegerInsertAction extends DataInsertAction {
    private final IntegerSettings integerSettings;


    /**
     * Constructs a new {@code IntegerInsertAction} that uses the singleton {@code IntegerSettings} instance.
     */
    public IntegerInsertAction() {
        this.integerSettings = IntegerSettings.getInstance();
    }

    /**
     * Constructs a new {@code IntegerInsertAction} that uses the given {@code IntegerSettings} instance.
     *
     * @param integerSettings the settings to use for generating integers
     */
    IntegerInsertAction(final @NotNull IntegerSettings integerSettings) {
        this.integerSettings = integerSettings;
    }


    @Override
    protected String getName() {
        return "Insert Integer";
    }

    /**
     * Returns a random integer between the minimum and maximum value, inclusive.
     *
     * @return a random integer between the minimum and maximum value, inclusive
     */
    @Override
    public String generateString() {
        final long randomValue = ThreadLocalRandom.current()
                .nextLong(integerSettings.getMinValue(), integerSettings.getMaxValue() + 1);

        return convertToString(randomValue);
    }


    /**
     * Returns a nicely formatted representation of a long.
     *
     * @param integer a {@code long}
     * @return a nicely formatted representation of a long
     */
    private String convertToString(final long integer) {
        if (integerSettings.getBase() != IntegerSettings.DECIMAL_BASE) {
            return Long.toString(integer, integerSettings.getBase());
        }


        final DecimalFormat format = new DecimalFormat();
        format.setGroupingUsed(integerSettings.getGroupingSeparator() != '\0');

        final DecimalFormatSymbols symbols = format.getDecimalFormatSymbols();
        symbols.setGroupingSeparator(integerSettings.getGroupingSeparator());
        format.setMinimumFractionDigits(0);
        format.setMaximumFractionDigits(0);
        format.setDecimalFormatSymbols(symbols);

        return format.format(integer);
    }


    /**
     * Inserts an array of integers.
     */
    public final class ArrayAction extends DataArrayInsertAction {
        /**
         * Constructs a new {@code ArrayAction} for integers.
         */
        public ArrayAction() {
            super(IntegerInsertAction.this);
        }


        @Override
        protected String getName() {
            return "Insert Integer Array";
        }
    }
}

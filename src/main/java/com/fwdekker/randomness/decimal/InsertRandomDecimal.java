package com.fwdekker.randomness.decimal;

import com.fwdekker.randomness.InsertRandomSomething;
import com.fwdekker.randomness.array.ArraySettings;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.concurrent.ThreadLocalRandom;
import org.jetbrains.annotations.NotNull;


/**
 * Generates a random integer based on the settings in {@link DecimalSettings}.
 */
public final class InsertRandomDecimal extends InsertRandomSomething {
    private final DecimalSettings decimalSettings;


    /**
     * Constructs a new {@code InsertRandomDecimal} that uses the singleton {@code DecimalSettings} instance.
     */
    public InsertRandomDecimal() {
        this.decimalSettings = DecimalSettings.getInstance();
    }

    /**
     * Constructs a new {@code InsertRandomDecimal} that uses the given {@code DecimalSettings} instance.
     *
     * @param arraySettings   the settings to use for generating arrays
     * @param decimalSettings the settings to use for generating decimals
     */
    InsertRandomDecimal(final @NotNull ArraySettings arraySettings, final @NotNull DecimalSettings decimalSettings) {
        super(arraySettings);

        this.decimalSettings = decimalSettings;
    }


    @Override
    protected String getName() {
        return "Insert Random Decimal";
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
}

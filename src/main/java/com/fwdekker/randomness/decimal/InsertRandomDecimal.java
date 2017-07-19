package com.fwdekker.randomness.decimal;

import com.fwdekker.randomness.InsertRandomSomething;
import java.math.BigDecimal;
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
     * @param decimalSettings the settings to use for generating decimals
     */
    InsertRandomDecimal(@NotNull final DecimalSettings decimalSettings) {
        this.decimalSettings = decimalSettings;
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
     * @param decimal a double
     * @return a nicely formatted representation of a double
     * @see <a href="https://stackoverflow.com/a/154354/">StackOverflow answer</a>
     */
    private String convertToString(final double decimal) {
        return new BigDecimal(String.valueOf(decimal))
                .setScale(decimalSettings.getDecimalCount(), BigDecimal.ROUND_HALF_UP).toString();
    }
}

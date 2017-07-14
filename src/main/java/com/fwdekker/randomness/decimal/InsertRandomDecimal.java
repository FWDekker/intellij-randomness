package com.fwdekker.randomness.decimal;

import com.fwdekker.randomness.InsertRandomSomething;
import java.math.BigDecimal;
import java.security.SecureRandom;
import java.util.Random;


/**
 * Generates a random integer based on the settings in {@link DecimalSettings}.
 */
final class InsertRandomDecimal extends InsertRandomSomething {
    private static final Random RANDOM = new SecureRandom();

    private final DecimalSettings decimalSettings = DecimalSettings.getInstance();


    /**
     * Returns a random integer between the minimum and maximum value, inclusive.
     *
     * @return a random integer between the minimum and maximum value, inclusive
     */
    @Override
    public String generateString() {
        final double range = decimalSettings.getMaxValue() - decimalSettings.getMinValue();
        final double randomValue = decimalSettings.getMinValue() + RANDOM.nextDouble() * range;

        return convertToString(randomValue);
    }


    /**
     * Returns the string nicely formatted representation of a double.
     *
     * @param decimal a double
     * @return the string nicely formatted representation of a double
     * @see <a href="https://stackoverflow.com/a/154354/">StackOverflow answer</a>
     */
    private String convertToString(final double decimal) {
        return new BigDecimal(String.valueOf(decimal))
                .setScale(decimalSettings.getDecimalCount(), BigDecimal.ROUND_HALF_UP).toString();
    }
}

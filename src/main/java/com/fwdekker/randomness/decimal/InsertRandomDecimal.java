package com.fwdekker.randomness.decimal;

import com.fwdekker.randomness.InsertRandomSomething;

import java.math.BigDecimal;
import java.util.Random;


/**
 * Generates a random integer based on the settings in {@link DecimalSettings}.
 */
final class InsertRandomDecimal extends InsertRandomSomething {
    private static final Random RANDOM = new Random();


    /**
     * Returns a random integer between the minimum and maximum value, inclusive.
     *
     * @return a random integer between the minimum and maximum value, inclusive
     */
    @Override
    public String generateString() {
        final double range = DecimalSettings.getMaxValue() - DecimalSettings.getMinValue();
        final double randomValue = DecimalSettings.getMinValue() + RANDOM.nextDouble() * range;

        return convertToString(randomValue);
    }


    /**
     * Returns the string nicely formatted representation of a double.
     *
     * @param d a double
     * @return the string nicely formatted representation of a double
     * @see <a href="https://stackoverflow.com/a/154354/">StackOverflow answer</a>
     */
    private String convertToString(final double d) {
        return new BigDecimal(String.valueOf(d))
                .setScale(DecimalSettings.getDecimalCount(), BigDecimal.ROUND_HALF_UP).toString();
    }
}

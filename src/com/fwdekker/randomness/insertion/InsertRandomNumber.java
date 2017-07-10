package com.fwdekker.randomness.insertion;

import java.util.Random;


/**
 * Generates a random number based on adjustable parameters.
 */
public class InsertRandomNumber extends InsertRandomSomething {
    private static final Random RANDOM = new Random();

    /**
     * The minimum value to be generated, inclusive.
     */
    private static int minValue = 0;
    /**
     * The maximum value to be generated, inclusive.
     */
    private static int maxValue = 1000;


    /**
     * Returns a random number between the minimum and maximum value, inclusive.
     *
     * @return a random number between the minimum and maximum value, inclusive
     */
    @Override
    String generateString() {
        final int range = maxValue - minValue;
        return Integer.toString(minValue + RANDOM.nextInt(range + 1));
    }


    /**
     * Returns the minimum value to be generated, inclusive.
     *
     * @return the minimum value to be generated, inclusive
     */
    public static int getMinValue() {
        return minValue;
    }

    /**
     * Sets the minimum value to be generated.
     *
     * @param minValue the minimum value to be generated, inclusive
     */
    public static void setMinValue(final int minValue) {
        InsertRandomNumber.minValue = minValue;
    }

    /**
     * Returns the maximum value to be generated, inclusive.
     *
     * @return the maximum value to be generated, inclusive
     */
    public static int getMaxValue() {
        return maxValue;
    }

    /**
     * Sets the maximum value to be generated.
     *
     * @param maxValue the maximum value to be generated, inclusive
     */
    public static void setMaxValue(final int maxValue) {
        InsertRandomNumber.maxValue = maxValue;
    }
}

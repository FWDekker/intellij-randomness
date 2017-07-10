package com.fwdekker.randomness.decimal;


/**
 * Contains settings for generating random decimals.
 */
public final class DecimalSettings {
    private static final double DEFAULT_MIN_VALUE = 0.0;
    private static final double DEFAULT_MAX_VALUE = 1000.0;
    private static final int DEFAULT_DECIMAL_COUNT = 2;

    /**
     * The minimum value to be generated, inclusive.
     */
    private static double minValue = DEFAULT_MIN_VALUE;
    /**
     * The maximum value to be generated, inclusive.
     */
    private static double maxValue = DEFAULT_MAX_VALUE;
    /**
     * The number of decimals to display.
     */
    private static int decimalCount = DEFAULT_DECIMAL_COUNT;


    /**
     * Private to prevent instantiation.
     */
    private DecimalSettings() {
    }


    /**
     * Returns the minimum value to be generated, inclusive.
     *
     * @return the minimum value to be generated, inclusive
     */
    static double getMinValue() {
        return minValue;
    }

    /**
     * Sets the minimum value to be generated.
     *
     * @param minValue the minimum value to be generated, inclusive
     */
    static void setMinValue(final double minValue) {
        DecimalSettings.minValue = minValue;
    }

    /**
     * Returns the maximum value to be generated, inclusive.
     *
     * @return the maximum value to be generated, inclusive
     */
    static double getMaxValue() {
        return maxValue;
    }

    /**
     * Sets the maximum value to be generated.
     *
     * @param maxValue the maximum value to be generated, inclusive
     */
    static void setMaxValue(final double maxValue) {
        DecimalSettings.maxValue = maxValue;
    }

    /**
     * Returns the number of decimals to display.
     *
     * @return the number of decimals to display
     */
    static int getDecimalCount() {
        return decimalCount;
    }

    /**
     * Sets the number of decimals to display.
     *
     * @param decimals the number of decimals to display
     */
    static void setDecimalCount(final int decimalCount) {
        DecimalSettings.decimalCount = decimalCount;
    }
}

package com.fwdekker.randomness.decimal;

import com.fwdekker.randomness.Settings;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;


/**
 * Contains settings for generating random decimals.
 */
@State(
        name = "DecimalSettings",
        storages = @Storage("$APP_CONFIG$/randomness.xml")
)
public final class DecimalSettings implements Settings<DecimalSettings> {
    private static final double DEFAULT_MIN_VALUE = 0.0;
    private static final double DEFAULT_MAX_VALUE = 1000.0;
    private static final int DEFAULT_DECIMAL_COUNT = 2;
    private static final char DEFAULT_GROUPING_SEPARATOR = '\0';
    private static final char DEFAULT_DECIMAL_SEPARATOR = '.';

    /**
     * The minimum value to be generated, inclusive.
     */
    private double minValue = DEFAULT_MIN_VALUE;
    /**
     * The maximum value to be generated, inclusive.
     */
    private double maxValue = DEFAULT_MAX_VALUE;
    /**
     * The number of decimals to display.
     */
    private int decimalCount = DEFAULT_DECIMAL_COUNT;
    /**
     * The character that should separate groups.
     */
    private char groupingSeparator = DEFAULT_GROUPING_SEPARATOR;
    /**
     * The character that should separate decimals.
     */
    private char decimalSeparator = DEFAULT_DECIMAL_SEPARATOR;


    /**
     * Returns the singleton {@code DecimalSettings} instance.
     *
     * @return the singleton {@code DecimalSettings} instance
     */
    public static DecimalSettings getInstance() {
        return ServiceManager.getService(DecimalSettings.class);
    }

    @Override
    public DecimalSettings getState() {
        return this;
    }

    @Override
    public void loadState(final DecimalSettings state) {
        XmlSerializerUtil.copyBean(state, this);
    }


    /**
     * Returns the minimum value to be generated, inclusive.
     *
     * @return the minimum value to be generated, inclusive
     */
    public double getMinValue() {
        return minValue;
    }

    /**
     * Sets the minimum value to be generated, inclusive.
     *
     * @param minValue the minimum value to be generated, inclusive
     */
    public void setMinValue(final double minValue) {
        this.minValue = minValue;
    }

    /**
     * Returns the maximum value to be generated, inclusive.
     *
     * @return the maximum value to be generated, inclusive
     */
    public double getMaxValue() {
        return maxValue;
    }

    /**
     * Sets the maximum value to be generated, inclusive.
     *
     * @param maxValue the maximum value to be generated, inclusive
     */
    public void setMaxValue(final double maxValue) {
        this.maxValue = maxValue;
    }

    /**
     * Returns the number of decimals to display.
     *
     * @return the number of decimals to display
     */
    public int getDecimalCount() {
        return decimalCount;
    }

    /**
     * Sets the number of decimals to display.
     *
     * @param decimalCount the number of decimals to display
     */
    public void setDecimalCount(final int decimalCount) {
        this.decimalCount = decimalCount;
    }

    /**
     * Returns the character that should separate groups.
     *
     * @return the character that should separate groups
     */
    public char getGroupingSeparator() {
        return groupingSeparator;
    }

    /**
     * Sets the character that should separate groups.
     *
     * @param groupingSeparator the character that should separate groups
     */
    public void setGroupingSeparator(final char groupingSeparator) {
        this.groupingSeparator = groupingSeparator;
    }

    /**
     * Sets the character that should separate groups.
     *
     * @param groupingSeparator a string of which the first character should separate groups. If the string is empty, no
     *                          character is used
     */
    public void setGroupingSeparator(final String groupingSeparator) {
        if ("".equals(groupingSeparator)) {
            this.groupingSeparator = '\0';
        } else {
            this.groupingSeparator = groupingSeparator.charAt(0);
        }
    }

    /**
     * Returns the character that should separate decimals.
     *
     * @return the character that should separate decimals
     */
    public char getDecimalSeparator() {
        return decimalSeparator;
    }

    /**
     * Sets the character that should separate decimals.
     *
     * @param decimalSeparator the character that should separate decimals
     */
    public void setDecimalSeparator(final char decimalSeparator) {
        this.decimalSeparator = decimalSeparator;
    }

    /**
     * Sets the character that should separate decimals.
     *
     * @param decimalSeparator a string of which the first character should separate decimals. If the string is empty,
     *                         no character is used
     */
    public void setDecimalSeparator(final String decimalSeparator) {
        if ("".equals(decimalSeparator)) {
            this.decimalSeparator = '\0';
        } else {
            this.decimalSeparator = decimalSeparator.charAt(0);
        }
    }
}

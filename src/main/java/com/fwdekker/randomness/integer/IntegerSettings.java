package com.fwdekker.randomness.integer;

import com.fwdekker.randomness.Settings;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;


/**
 * Contains settings for generating random integers.
 */
@State(
        name = "IntegerSettings",
        storages = @Storage("$APP_CONFIG$/randomness.xml")
)
public final class IntegerSettings extends Settings implements PersistentStateComponent<IntegerSettings> {
    private static final long DEFAULT_MIN_VALUE = 0L;
    private static final long DEFAULT_MAX_VALUE = 1000L;
    private static final char DEFAULT_GROUPING_SEPARATOR = '\0';

    /**
     * The minimum value to be generated, inclusive.
     */
    private long minValue = DEFAULT_MIN_VALUE;
    /**
     * The maximum value to be generated, inclusive.
     */
    private long maxValue = DEFAULT_MAX_VALUE;
    /**
     * The character that should separate groups.
     */
    private char groupingSeparator = DEFAULT_GROUPING_SEPARATOR;


    /**
     * Returns the singleton {@code IntegerSettings} instance.
     *
     * @return the singleton {@code IntegerSettings} instance
     */
    public static IntegerSettings getInstance() {
        return ServiceManager.getService(IntegerSettings.class);
    }

    @Override
    public IntegerSettings getState() {
        return this;
    }

    @Override
    public void loadState(@NotNull final IntegerSettings state) {
        XmlSerializerUtil.copyBean(state, this);
    }


    /**
     * Returns the minimum value to be generated, inclusive.
     *
     * @return the minimum value to be generated, inclusive
     */
    public long getMinValue() {
        return minValue;
    }

    /**
     * Sets the minimum value to be generated, inclusive.
     *
     * @param minValue the minimum value to be generated, inclusive
     */
    public void setMinValue(final long minValue) {
        this.minValue = minValue;
    }

    /**
     * Returns the maximum value to be generated, inclusive.
     *
     * @return the maximum value to be generated, inclusive
     */
    public long getMaxValue() {
        return maxValue;
    }

    /**
     * Sets the maximum value to be generated, inclusive.
     *
     * @param maxValue the maximum value to be generated, inclusive
     */
    public void setMaxValue(final long maxValue) {
        this.maxValue = maxValue;
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
}

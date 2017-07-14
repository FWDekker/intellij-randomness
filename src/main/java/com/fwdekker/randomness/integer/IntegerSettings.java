package com.fwdekker.randomness.integer;

import com.fwdekker.randomness.Settings;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;


/**
 * Contains settings for generating random integers.
 */
@State(
        name = "IntegerSettings",
        storages = @Storage(file = "$APP_CONFIG$/randomness.xml")
)
public final class IntegerSettings extends Settings implements PersistentStateComponent<IntegerSettings> {
    private static final int DEFAULT_MIN_VALUE = 0;
    private static final int DEFAULT_MAX_VALUE = 1000;

    /**
     * The minimum value to be generated, inclusive.
     */
    private int minValue = DEFAULT_MIN_VALUE;
    /**
     * The maximum value to be generated, inclusive.
     */
    private int maxValue = DEFAULT_MAX_VALUE;


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
    public void loadState(final IntegerSettings state) {
        XmlSerializerUtil.copyBean(state, this);
    }


    /**
     * Returns the minimum value to be generated, inclusive.
     *
     * @return the minimum value to be generated, inclusive
     */
    public int getMinValue() {
        return minValue;
    }

    /**
     * Sets the minimum value to be generated, inclusive.
     *
     * @param minValue the minimum value to be generated, inclusive
     */
    public void setMinValue(final int minValue) {
        this.minValue = minValue;
    }

    /**
     * Returns the maximum value to be generated, inclusive.
     *
     * @return the maximum value to be generated, inclusive
     */
    public int getMaxValue() {
        return maxValue;
    }

    /**
     * Sets the maximum value to be generated, inclusive.
     *
     * @param maxValue the maximum value to be generated, inclusive
     */
    public void setMaxValue(final int maxValue) {
        this.maxValue = maxValue;
    }
}

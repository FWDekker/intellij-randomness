package com.fwdekker.randomness.string;

import com.fwdekker.randomness.Settings;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;


/**
 * Contains settings for generating random strings.
 */
@State(
        name = "StringSettings",
        storages = @Storage(file = "$APP_CONFIG$/randomness.xml")
)
public final class StringSettings extends Settings implements PersistentStateComponent<StringSettings> {
    /**
     * The characters that may be used for generated strings.
     */
    public static final String ALPHABET = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    private static final int DEFAULT_MIN_LENGTH = 10;
    private static final int DEFAULT_MAX_LENGTH = 10;

    /**
     * The minimum length of a generated string, inclusive.
     */
    public int minLength = DEFAULT_MIN_LENGTH;
    /**
     * The maximum length of a generated string, inclusive.
     */
    public int maxLength = DEFAULT_MAX_LENGTH;
    /**
     * The string that encloses the generated string on both sides.
     */
    public String enclosure = "\"";
    /**
     * True if generated strings should be enclosed with quotation marks.
     */
    public boolean quotationMarksEnabled = true;


    /**
     * Private to prevent instantiation.
     */
    private StringSettings() {
    }


    /**
     * Returns the singleton {@code StringSettings} instance.
     *
     * @return the singleton {@code StringSettings} instance
     */
    public static StringSettings getInstance() {
        return ServiceManager.getService(StringSettings.class);
    }

    @Override
    public StringSettings getState() {
        return this;
    }

    @Override
    public void loadState(final StringSettings state) {
        XmlSerializerUtil.copyBean(state, this);
    }


    /**
     * Returns the minimum length of a generated string, inclusive.
     *
     * @return the minimum length of a generated string, inclusive
     */
    public int getMinLength() {
        return minLength;
    }

    /**
     * Sets the minimum length of a generated string, inclusive.
     *
     * @param minLength the minimum length of a generated string, inclusive
     */
    public void setMinLength(final int minLength) {
        this.minLength = minLength;
    }

    /**
     * Returns the maximum length of a generated string, inclusive.
     *
     * @return the maximum length of a generated string, inclusive
     */
    public int getMaxLength() {
        return maxLength;
    }

    /**
     * Sets the maximum length of a generated string, inclusive.
     *
     * @param maxLength the maximum length of a generated string, inclusive
     */
    public void setMaxLength(final int maxLength) {
        this.maxLength = maxLength;
    }

    /**
     * Returns the string that encloses the generated string on both sides.
     *
     * @return the string that encloses the generated string on both sides
     */
    public String getEnclosure() {
        return enclosure;
    }

    /**
     * Returns true if generated strings should be enclosed with quotation marks.
     *
     * @return true if generated strings should be enclosed with quotation marks
     */
    public boolean isQuotationMarksEnabled() {
        return quotationMarksEnabled;
    }

    /**
     * Sets the quotation marks property.
     *
     * @param quotationMarksEnabled true if generated strings should be enclosed with quotation marks
     */
    public void setQuotationMarksEnabled(final boolean quotationMarksEnabled) {
        this.quotationMarksEnabled = quotationMarksEnabled;
        this.enclosure = quotationMarksEnabled ? "\"" : "";
    }
}

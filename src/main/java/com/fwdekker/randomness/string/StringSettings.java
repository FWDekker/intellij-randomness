package com.fwdekker.randomness.string;

import com.fwdekker.randomness.Settings;
import com.fwdekker.randomness.CapitalizationMode;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;


/**
 * Contains settings for generating random strings.
 */
@State(
        name = "StringSettings",
        storages = @Storage("$APP_CONFIG$/randomness.xml")
)
public final class StringSettings extends Settings implements PersistentStateComponent<StringSettings> {
    private static final int DEFAULT_MIN_LENGTH = 3;
    private static final int DEFAULT_MAX_LENGTH = 8;

    /**
     * The minimum length of a generated string, inclusive.
     */
    private int minLength = DEFAULT_MIN_LENGTH;
    /**
     * The maximum length of a generated string, inclusive.
     */
    private int maxLength = DEFAULT_MAX_LENGTH;
    /**
     * The string that encloses the generated string on both sides.
     */
    private String enclosure = "\"";
    /**
     * The capitalization mode of the generated string.
     */
    private CapitalizationMode capitalization = CapitalizationMode.UPPER;
    /**
     * The alphabet to be used for generating strings.
     */
    private Set<Alphabet> alphabets = new HashSet<>(Arrays.asList(Alphabet.ALPHABET, Alphabet.DIGITS));


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
    public void loadState(final @NotNull StringSettings state) {
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
     * Sets the string that encloses the generated string on both sides.
     *
     * @param enclosure the string that encloses the generated string on both sides
     */
    public void setEnclosure(final String enclosure) {
        this.enclosure = enclosure;
    }

    /**
     * Returns the capitalization mode of the generated string.
     *
     * @return the capitalization mode of the generated string
     */
    public CapitalizationMode getCapitalization() {
        return capitalization;
    }

    /**
     * Sets the capitalization mode of the generated string.
     *
     * @param capitalization the capitalization mode of the generated string
     */
    public void setCapitalization(final CapitalizationMode capitalization) {
        this.capitalization = capitalization;
    }

    /**
     * Returns the alphabet to be used for generating strings.
     *
     * @return the alphabet to be used for generating strings
     */
    public Set<Alphabet> getAlphabets() {
        return alphabets;
    }

    /**
     * Sets the alphabet to be used for generating strings.
     *
     * @param alphabets the alphabet to be used for generating strings
     */
    public void setAlphabets(final Set<Alphabet> alphabets) {
        this.alphabets = alphabets;
    }
}

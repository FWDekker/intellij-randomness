package com.fwdekker.randomness.array;

import com.fwdekker.randomness.Settings;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import java.util.Collection;


/**
 * Contains settings for generating random arrays of other types of random values.
 */
@State(
        name = "ArraySettings",
        storages = @Storage("$APP_CONFIG$/randomness.xml")
)
public final class ArraySettings extends Settings implements PersistentStateComponent<ArraySettings> {
    private static final int DEFAULT_COUNT = 5;
    private static final String DEFAULT_BRACKETS = "[]";
    private static final String DEFAULT_SEPARATOR = ",";
    private static final boolean DEFAULT_SPACE_AFTER_SEPARATOR = true;

    /**
     * The number of elements to generate.
     */
    private int count = DEFAULT_COUNT;
    /**
     * The brackets to surround arrays with.
     */
    private String brackets = DEFAULT_BRACKETS;
    /**
     * The separator to place between generated elements.
     */
    private String separator = DEFAULT_SEPARATOR;
    /**
     * {@code true} iff. a space should be placed after each separator.
     */
    private boolean spaceAfterSeparator = DEFAULT_SPACE_AFTER_SEPARATOR;


    /**
     * Returns the singleton {@code ArraySettings} instance.
     *
     * @return the singleton {@code ArraySettings} instance
     */
    public static ArraySettings getInstance() {
        return ServiceManager.getService(ArraySettings.class);
    }

    @Override
    public ArraySettings getState() {
        return this;
    }

    @Override
    public void loadState(final ArraySettings state) {
        XmlSerializerUtil.copyBean(state, this);
    }


    /**
     * Returns the number of elements to generate.
     *
     * @return the number of elements to generate
     */
    public int getCount() {
        return count;
    }

    /**
     * Sets the number of elements to generate.
     *
     * @param count the number of elements to generate
     */
    public void setCount(final int count) {
        this.count = count;
    }

    /**
     * The brackets to surround arrays with.
     *
     * @return the brackets to surround arrays with
     */
    public String getBrackets() {
        return brackets;
    }

    /**
     * Sets the brackets to surround arrays with.
     *
     * @param brackets the brackets to surround arrays with
     */
    public void setBrackets(final String brackets) {
        this.brackets = brackets;
    }

    /**
     * Returns the separator to place between generated elements.
     *
     * @return the separator to place between generated elements
     */
    public String getSeparator() {
        return separator;
    }

    /**
     * Sets the separator to place between generated elements.
     *
     * @param separator the separator to place between generated elements
     */
    public void setSeparator(final String separator) {
        this.separator = separator;
    }

    /**
     * Returns {@code true} iff. a space should be placed after each separator.
     *
     * @return {@code true} iff. a space should be placed after each separator
     */
    public boolean isSpaceAfterSeparator() {
        return spaceAfterSeparator;
    }

    /**
     * Sets whether a space should be placed after each separator.
     *
     * @param spaceAfterSeparator {@code true} iff. a space should be placed after each separator
     */
    public void setSpaceAfterSeparator(final boolean spaceAfterSeparator) {
        this.spaceAfterSeparator = spaceAfterSeparator;
    }


    /**
     * Turns a collection of strings into a string representation as defined by this {@code ArraySettings}'
     * settings.
     *
     * @param strings the strings to array-ify
     * @return a string representation as defined by this {@code ArraySettings}' settings
     */
    public String arrayify(final Collection<String> strings) {
        final String leftBracket = brackets.length() >= 2 ? Character.toString(brackets.charAt(0)) : "";
        final String rightBracket = brackets.length() >= 2 ? Character.toString(brackets.charAt(1)) : "";
        final String separator = this.separator + (spaceAfterSeparator ? " " : "");

        return leftBracket + String.join(separator, strings) + rightBracket;
    }
}

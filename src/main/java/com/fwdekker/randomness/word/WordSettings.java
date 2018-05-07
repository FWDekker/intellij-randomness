package com.fwdekker.randomness.word;

import com.fwdekker.randomness.Settings;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


/**
 * Contains settings for generating random words.
 */
@State(
        name = "WordSettings",
        storages = @Storage("$APP_CONFIG$/randomness.xml")
)
public final class WordSettings extends Settings implements PersistentStateComponent<WordSettings> {
    private static final int DEFAULT_MIN_LENGTH = 3;
    private static final int DEFAULT_MAX_LENGTH = 8;

    /**
     * The minimum length of a generated word, inclusive.
     */
    private int minLength = DEFAULT_MIN_LENGTH;
    /**
     * The maximum length of a generated word, inclusive.
     */
    private int maxLength = DEFAULT_MAX_LENGTH;
    /**
     * The string that encloses the generated word on both sides.
     */
    private String enclosure = "\"";
    /**
     * The way in which the generated word should be capitalized.
     */
    private CapitalizationMode capitalization = CapitalizationMode.NORMAL;
    /**
     * The list of all dictionaries provided by the plugin.
     */
    private Set<String> bundledDictionaries = new HashSet<>(Arrays.asList(Dictionary.DEFAULT_DICTIONARY_FILE));
    /**
     * The list of all dictionaries registered by the user.
     */
    private Set<String> userDictionaries = new HashSet<>();
    /**
     * The list of bundled dictionaries that are currently active.
     */
    private Set<String> activeBundledDictionaries = new HashSet<>(Arrays.asList(Dictionary.DEFAULT_DICTIONARY_FILE));
    /**
     * The list of user dictionaries that are currently active.
     */
    private Set<String> activeUserDictionaries = new HashSet<>();


    /**
     * Returns the singleton {@code WordSettings} instance.
     *
     * @return the singleton {@code WordSettings} instance
     */
    public static WordSettings getInstance() {
        return ServiceManager.getService(WordSettings.class);
    }

    @Override
    public WordSettings getState() {
        return this;
    }

    @Override
    public void loadState(final @NotNull WordSettings state) {
        XmlSerializerUtil.copyBean(state, this);
    }


    /**
     * Returns the minimum length of a generated word, inclusive.
     *
     * @return the minimum length of a generated word, inclusive
     */
    public int getMinLength() {
        return minLength;
    }

    /**
     * Sets the minimum length of a generated word, inclusive.
     *
     * @param minLength the minimum length of a generated word, inclusive
     */
    public void setMinLength(final int minLength) {
        this.minLength = minLength;
    }

    /**
     * Returns the maximum length of a generated word, inclusive.
     *
     * @return the maximum length of a generated word, inclusive
     */
    public int getMaxLength() {
        return maxLength;
    }

    /**
     * Sets the maximum length of a generated word, inclusive.
     *
     * @param maxLength the maximum length of a generated word, inclusive
     */
    public void setMaxLength(final int maxLength) {
        this.maxLength = maxLength;
    }

    /**
     * Returns the string that encloses the generated word on both sides.
     *
     * @return the string that encloses the generated word on both sides
     */
    public String getEnclosure() {
        return enclosure;
    }

    /**
     * Sets the string that encloses the generated word on both sides.
     *
     * @param enclosure the string that encloses the generated word on both sides
     */
    public void setEnclosure(final String enclosure) {
        this.enclosure = enclosure;
    }

    /**
     * Returns the way in which the generated word should be capitalized.
     *
     * @return the way in which the generated word should be capitalized
     */
    public CapitalizationMode getCapitalization() {
        return capitalization;
    }

    /**
     * Sets the way in which the generated word should be capitalized.
     *
     * @param capitalization the way in which the generated word should be capitalized
     */
    public void setCapitalization(final CapitalizationMode capitalization) {
        this.capitalization = capitalization;
    }

    /**
     * Returns the list of all dictionaries provided by the plugin.
     *
     * @return the list of all dictionaries provided by the plugin
     */
    public Set<String> getBundledDictionaries() {
        return bundledDictionaries;
    }

    /**
     * Sets the list of all dictionaries provided by the plugin.
     *
     * @param bundledDictionaries the list of all dictionaries provided by the plugin
     */
    public void setBundledDictionaries(final Set<String> bundledDictionaries) {
        this.bundledDictionaries = bundledDictionaries;
    }

    /**
     * Returns the list of all dictionaries registered by the user.
     *
     * @return the list of all dictionaries registered by the user
     */
    public Set<String> getUserDictionaries() {
        return userDictionaries;
    }

    /**
     * Sets the list of all dictionaries registered by the user.
     *
     * @param userDictionaries the list of all dictionaries registered by the user
     */
    public void setUserDictionaries(final Set<String> userDictionaries) {
        this.userDictionaries = userDictionaries;
    }

    /**
     * Returns the list of bundled dictionaries that are currently active.
     *
     * @return the list of bundled dictionaries that are currently active
     */
    public Set<String> getActiveBundledDictionaries() {
        return activeBundledDictionaries;
    }

    /**
     * Sets the list of bundled dictionaries that are currently active.
     *
     * @param activeBundledDictionaries the list of bundled dictionaries that are currently active
     */
    public void setActiveBundledDictionaries(final Set<String> activeBundledDictionaries) {
        this.activeBundledDictionaries = activeBundledDictionaries;
    }

    /**
     * Returns the list of user dictionaries that are currently active.
     *
     * @return the list of user dictionaries that are currently active
     */
    public Set<String> getActiveUserDictionaries() {
        return activeUserDictionaries;
    }

    /**
     * Sets the list of user dictionaries that are currently active.
     *
     * @param activeUserDictionaries the list of user dictionaries that are currently active
     */
    public void setActiveUserDictionaries(final Set<String> activeUserDictionaries) {
        this.activeUserDictionaries = activeUserDictionaries;
    }


    /**
     * Returns the list of all dictionaries.
     *
     * @return the list of all dictionaries
     */
    public List<Dictionary> getDictionaries() {
        final List<Dictionary> dictionaries = new ArrayList<>();

        dictionaries.addAll(bundledDictionaries.stream()
                .map(Dictionary.BundledDictionary::get)
                .collect(Collectors.toList()));
        dictionaries.addAll(userDictionaries.stream()
                .map(Dictionary.UserDictionary::get)
                .collect(Collectors.toList()));

        return dictionaries;
    }

    /**
     * Returns the list of all dictionaries that are currently active.
     *
     * @return the list of all dictionaries that are currently active
     */
    public List<Dictionary> getActiveDictionaries() {
        final List<Dictionary> dictionaries = new ArrayList<>();

        dictionaries.addAll(activeBundledDictionaries.stream()
                .map(Dictionary.BundledDictionary::get)
                .collect(Collectors.toList()));
        dictionaries.addAll(activeUserDictionaries.stream()
                .map(Dictionary.UserDictionary::get)
                .collect(Collectors.toList()));

        return dictionaries;
    }
}

package com.fwdekker.randomness.word;

import com.fwdekker.randomness.Settings;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;


/**
 * Contains settings for generating random words.
 */
@State(
        name = "WordSettings",
        storages = @Storage("$APP_CONFIG$/randomness.xml")
)
public final class WordSettings extends Settings implements PersistentStateComponent<WordSettings> {
    private static final int DEFAULT_MIN_LENGTH = 10;
    private static final int DEFAULT_MAX_LENGTH = 10;

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
    private Set<String> resourceDictionaries = new HashSet<>(Arrays.asList(Dictionary.DEFAULT_DICTIONARY_FILE));
    private Set<String> localDictionaries = new HashSet();
    private Set<String> selectedResourceDictionaries = new HashSet<>(Arrays.asList(Dictionary.DEFAULT_DICTIONARY_FILE));
    private Set<String> selectedLocalDictionaries = new HashSet<>();


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

    public Set<String> getResourceDictionaries() {
        return resourceDictionaries;
    }

    public void setResourceDictionaries(Set<String> resourceDictionaries) {
        this.resourceDictionaries = resourceDictionaries;
    }

    public Set<String> getLocalDictionaries() {
        return localDictionaries;
    }

    public void setLocalDictionaries(Set<String> localDictionaries) {
        this.localDictionaries = localDictionaries;
    }

    public Set<String> getSelectedResourceDictionaries() {
        return selectedResourceDictionaries;
    }

    public void setSelectedResourceDictionaries(Set<String> selectedResourceDictionaries) {
        this.selectedResourceDictionaries = selectedResourceDictionaries;
    }

    public Set<String> getSelectedLocalDictionaries() {
        return selectedLocalDictionaries;
    }

    public void setSelectedLocalDictionaries(Set<String> selectedLocalDictionaries) {
        this.selectedLocalDictionaries = selectedLocalDictionaries;
    }

    public Set<Dictionary> getSelectedDictionaries() {
        final Set<Dictionary> dictionaries = new HashSet<>();

        dictionaries.addAll(selectedResourceDictionaries.stream()
                                    .map(dictionary -> new Dictionary.ResourceDictionary(dictionary))
                                    .collect(Collectors.toList()));
        dictionaries.addAll(selectedLocalDictionaries.stream()
                                    .map(dictionary -> new Dictionary.LocalDictionary(dictionary))
                                    .collect(Collectors.toList()));

        return dictionaries;
    }

    public Dictionary getSelectedDictionariesCombined() {
        final Set<Dictionary> selectedDictionaries = getSelectedDictionaries();
        Dictionary combinedDictionary = (Dictionary) selectedDictionaries.toArray()[0];

        for (final Dictionary dictionary : selectedDictionaries) {
            combinedDictionary = combinedDictionary.combineWith(dictionary);
        }

        return combinedDictionary;
    }
}

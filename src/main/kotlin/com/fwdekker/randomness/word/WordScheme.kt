package com.fwdekker.randomness.word

import com.fwdekker.randomness.CapitalizationMode
import com.fwdekker.randomness.DataGenerationException
import com.fwdekker.randomness.Scheme
import com.intellij.util.xmlb.annotations.Transient


/**
 * Contains settings for generating random words.
 *
 * @property minLength The minimum length of the generated word, inclusive.
 * @property maxLength The maximum length of the generated word, inclusive.
 * @property enclosure The string that encloses the generated word on both sides.
 * @property capitalization The way in which the generated word should be capitalized.
 * @property activeBundledDictionaryFiles The list of bundled dictionary files that are currently active.
 * @property activeUserDictionaryFiles The list of user dictionary files that are currently active.
 */
data class WordScheme(
    var minLength: Int = DEFAULT_MIN_LENGTH,
    var maxLength: Int = DEFAULT_MAX_LENGTH,
    var enclosure: String = DEFAULT_ENCLOSURE,
    var capitalization: CapitalizationMode = DEFAULT_CAPITALIZATION,
    var activeBundledDictionaryFiles: MutableSet<String> = DEFAULT_ACTIVE_BUNDLED_DICTIONARY_FILES,
    var activeUserDictionaryFiles: MutableSet<String> = DEFAULT_ACTIVE_USER_DICTIONARY_FILES
) : Scheme<WordScheme>() {
    /**
     * A mutable view of the filenames of the files in [activeBundledDictionaryFiles].
     */
    var activeBundledDictionaries: Set<DictionaryReference>
        @Transient
        get() = activeBundledDictionaryFiles.map { DictionaryReference(true, it) }.toSet()
        set(value) {
            activeBundledDictionaryFiles = value.map { it.filename }.toMutableSet()
        }

    /**
     * A mutable view of the filenames of the files in [activeUserDictionaryFiles].
     */
    var activeUserDictionaries: Set<DictionaryReference>
        @Transient
        get() = activeUserDictionaryFiles.map { DictionaryReference(false, it) }.toSet()
        set(value) {
            activeUserDictionaryFiles = value.map { it.filename }.toMutableSet()
        }


    /**
     * Returns random words from the dictionaries in `settings`.
     *
     * @param count the number of words to generate
     * @return random words from the dictionaries in `settings`
     * @throws InvalidDictionaryException if no words could be found using the settings in `settings`
     */
    override fun generateStrings(count: Int): List<String> {
        val dictionaries = (activeBundledDictionaries + activeUserDictionaries)
            .ifEmpty { throw DataGenerationException("There are no active dictionaries.") }

        val words =
            try {
                dictionaries.flatMap { it.words }
            } catch (e: InvalidDictionaryException) {
                throw DataGenerationException(e.message, e)
            }
                .ifEmpty { throw DataGenerationException("All active dictionaries are empty.") }
                .filter { it.length in minLength..maxLength }
                .toSet()
                .ifEmpty { throw DataGenerationException("There are no words within the configured length range.") }

        return List(count) { words.random(random) }
            .map { capitalization.transform(it) }
            .map { enclosure + it + enclosure }
    }


    override fun deepCopy() =
        copy().also {
            it.activeBundledDictionaries = activeBundledDictionaries.map(DictionaryReference::copy).toSet()
            it.activeUserDictionaries = activeUserDictionaries.map(DictionaryReference::copy).toSet()
        }


    /**
     * Holds constants.
     */
    companion object {
        /**
         * The default value of the [minLength][minLength] field.
         */
        const val DEFAULT_MIN_LENGTH = 3

        /**
         * The default value of the [maxLength][maxLength] field.
         */
        const val DEFAULT_MAX_LENGTH = 8

        /**
         * The default value of the [enclosure][enclosure] field.
         */
        const val DEFAULT_ENCLOSURE = "\""

        /**
         * The default value of the [capitalization][capitalization] field.
         */
        val DEFAULT_CAPITALIZATION = CapitalizationMode.RETAIN

        /**
         * The default value of the [activeBundledDictionaryFiles][activeBundledDictionaryFiles] field.
         */
        val DEFAULT_ACTIVE_BUNDLED_DICTIONARY_FILES: MutableSet<String>
            get() = mutableSetOf(BundledDictionary.SIMPLE_DICTIONARY)

        /**
         * The default value of the [activeUserDictionaryFiles][activeUserDictionaryFiles] field.
         */
        val DEFAULT_ACTIVE_USER_DICTIONARY_FILES: MutableSet<String>
            get() = mutableSetOf()
    }
}

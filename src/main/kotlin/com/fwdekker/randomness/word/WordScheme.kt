package com.fwdekker.randomness.word

import com.fwdekker.randomness.CapitalizationMode
import com.fwdekker.randomness.DataGenerationException
import com.fwdekker.randomness.Scheme
import com.fwdekker.randomness.uds.collectionToString


/**
 * Contains settings for generating random words.
 *
 * @property minLength The minimum length of the generated word, inclusive.
 * @property maxLength The maximum length of the generated word, inclusive.
 * @property enclosure The string that encloses the generated word on both sides.
 * @property capitalization The way in which the generated word should be capitalized.
 * @property bundledDictionaries The list of all dictionary files provided by the plugin.
 * @property userDictionaries The list of all dictionary files registered by the user.
 * @property activeBundledDictionaries The list of bundled dictionary files that are currently active; a subset of
 * [bundledDictionaries].
 * @property activeUserDictionaries The list of user dictionary files that are currently active; a subset of
 * [userDictionaries].
 */
data class WordScheme(
    var minLength: Int = DEFAULT_MIN_LENGTH,
    var maxLength: Int = DEFAULT_MAX_LENGTH,
    var enclosure: String = DEFAULT_ENCLOSURE,
    var capitalization: CapitalizationMode = DEFAULT_CAPITALIZATION,
    var bundledDictionaries: Set<DictionaryReference> = DEFAULT_BUNDLED_DICTIONARIES.toMutableSet(),
    var activeBundledDictionaries: Set<DictionaryReference> = DEFAULT_ACTIVE_BUNDLED_DICTIONARIES.toMutableSet(),
    var userDictionaries: Set<DictionaryReference> = DEFAULT_USER_DICTIONARIES.toMutableSet(),
    var activeUserDictionaries: Set<DictionaryReference> = DEFAULT_ACTIVE_USER_DICTIONARIES.toMutableSet()
) : Scheme<WordScheme>() {
    override val descriptor =
        "%Word[" +
            "minLength=$minLength, " +
            "maxLength=$maxLength, " +
            "enclosure=$enclosure, " +
            "capitalization=$capitalization, " +
            "bundledDictionaries=${collectionToString(bundledDictionaries)}, " +
            "activeBundledDictionaries=${collectionToString(activeBundledDictionaries)}, " +
            "userDictionaries=${collectionToString(userDictionaries)}, " +
            "activeUserDictionaries=${collectionToString(activeUserDictionaries)}" +
            "]"


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
         * The default value of the [bundledDictionaries][bundledDictionaries] field.
         */
        val DEFAULT_BUNDLED_DICTIONARIES = setOf(BundledDictionary.SIMPLE_DICTIONARY)

        /**
         * The default value of the [activeBundledDictionaries][activeBundledDictionaries] field.
         */
        val DEFAULT_ACTIVE_BUNDLED_DICTIONARIES = setOf(BundledDictionary.SIMPLE_DICTIONARY)

        /**
         * The default value of the [userDictionaries][userDictionaries] field.
         */
        val DEFAULT_USER_DICTIONARIES = setOf<DictionaryReference>()

        /**
         * The default value of the [activeUserDictionaries][activeUserDictionaries] field.
         */
        val DEFAULT_ACTIVE_USER_DICTIONARIES = setOf<DictionaryReference>()
    }
}

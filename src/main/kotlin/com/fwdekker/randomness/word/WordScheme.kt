package com.fwdekker.randomness.word

import com.fwdekker.randomness.CapitalizationMode
import com.fwdekker.randomness.Scheme
import com.fwdekker.randomness.SettingsState
import com.fwdekker.randomness.array.ArraySchemeDecorator
import com.intellij.util.xmlb.annotations.Transient
import icons.RandomnessIcons


/**
 * Contains settings for generating random words.
 *
 * @property dictionarySettings Persistent storage of available dictionaries.
 * @property minLength The minimum length of the generated word, inclusive.
 * @property maxLength The maximum length of the generated word, inclusive.
 * @property enclosure The string that encloses the generated word on both sides.
 * @property capitalization The way in which the generated word should be capitalized.
 * @property activeBundledDictionaryFiles The list of bundled dictionary files that are currently active.
 * @property activeUserDictionaryFiles The list of user dictionary files that are currently active.
 * @property decorator Settings that determine whether the output should be an array of values.
 */
data class WordScheme(
    @Transient
    var dictionarySettings: DictionarySettings = DictionarySettings.default,
    var minLength: Int = DEFAULT_MIN_LENGTH,
    var maxLength: Int = DEFAULT_MAX_LENGTH,
    var enclosure: String = DEFAULT_ENCLOSURE,
    var capitalization: CapitalizationMode = DEFAULT_CAPITALIZATION,
    var activeBundledDictionaryFiles: MutableSet<String> = DEFAULT_ACTIVE_BUNDLED_DICTIONARY_FILES.toMutableSet(),
    var activeUserDictionaryFiles: MutableSet<String> = DEFAULT_ACTIVE_USER_DICTIONARY_FILES.toMutableSet(),
    override var decorator: ArraySchemeDecorator = ArraySchemeDecorator()
) : Scheme() {
    @Transient
    override val name = "Word"
    override val icons = RandomnessIcons.Word

    /**
     * A view of the filenames of the files in [activeBundledDictionaryFiles].
     */
    var activeBundledDictionaries: Set<DictionaryReference>
        @Transient
        get() = activeBundledDictionaryFiles.map { DictionaryReference(true, it) }.toSet()
        set(value) {
            activeBundledDictionaryFiles = value.map { it.filename }.toMutableSet()
        }

    /**
     * A view of the filenames of the files in [activeUserDictionaryFiles].
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
    override fun generateUndecoratedStrings(count: Int): List<String> {
        val words =
            (activeBundledDictionaries + activeUserDictionaries)
                .flatMap { it.words }
                .filter { it.length in minLength..maxLength }
                .toSet()

        return List(count) { words.random(random) }
            .map { capitalization.transform(it) }
            .map { enclosure + it + enclosure }
    }

    override fun setSettingsState(settingsState: SettingsState) {
        super.setSettingsState(settingsState)
        dictionarySettings = settingsState.dictionarySettings
    }


    override fun doValidate(): String? {
        dictionarySettings.doValidate()?.also { return it }

        val words = (activeBundledDictionaries + activeUserDictionaries).flatMap { it.words }
        val minWordLength = words.map { it.length }.minOrNull() ?: 1
        val maxWordLength = words.map { it.length }.maxOrNull() ?: Integer.MAX_VALUE

        return when {
            minLength < MIN_LENGTH ->
                "Minimum length should not be smaller than $MIN_LENGTH."
            minLength > maxLength ->
                "Minimum length should not be larger than maximum length."
            (activeBundledDictionaryFiles + activeUserDictionaryFiles).isEmpty() ->
                "Activate at least one dictionary."
            minLength > maxWordLength ->
                "The longest word in the selected dictionaries is $maxWordLength characters. " +
                    "Set the minimum length to a value less than or equal to $maxWordLength."
            maxLength < minWordLength ->
                "The shortest word in the selected dictionaries is $minWordLength characters. " +
                    "Set the maximum length to a value less than or equal to $minWordLength."
            else -> decorator.doValidate()
        }
    }

    override fun deepCopy(retainUuid: Boolean) =
        copy(dictionarySettings = dictionarySettings, decorator = decorator.deepCopy(retainUuid))
            .also {
                if (retainUuid) it.uuid = this.uuid

                it.activeBundledDictionaries = activeBundledDictionaries.map(DictionaryReference::copy).toSet()
                it.activeUserDictionaries = activeUserDictionaries.map(DictionaryReference::copy).toSet()
            }


    /**
     * Holds constants.
     */
    companion object {
        /**
         * The smallest valid value of the [minLength] field.
         */
        const val MIN_LENGTH = 1

        /**
         * The largest valid difference between the [minLength] and [maxLength] fields.
         */
        const val MAX_LENGTH_DIFFERENCE = Int.MAX_VALUE

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
        val DEFAULT_ACTIVE_BUNDLED_DICTIONARY_FILES: Set<String>
            get() = setOf(BundledDictionary.SIMPLE_DICTIONARY)

        /**
         * The default value of the [activeUserDictionaryFiles][activeUserDictionaryFiles] field.
         */
        val DEFAULT_ACTIVE_USER_DICTIONARY_FILES: Set<String>
            get() = setOf()
    }
}

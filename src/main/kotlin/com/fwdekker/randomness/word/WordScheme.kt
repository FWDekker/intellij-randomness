package com.fwdekker.randomness.word

import com.fwdekker.randomness.Box
import com.fwdekker.randomness.CapitalizationMode
import com.fwdekker.randomness.Scheme
import com.fwdekker.randomness.SettingsState
import com.fwdekker.randomness.array.ArraySchemeDecorator
import com.intellij.util.xmlb.annotations.Transient
import com.intellij.util.xmlb.annotations.XCollection
import icons.RandomnessIcons


/**
 * Contains settings for generating random words.
 *
 * @property minLength The minimum length of the generated word, inclusive.
 * @property maxLength The maximum length of the generated word, inclusive.
 * @property enclosure The string that encloses the generated word on both sides.
 * @property capitalization The way in which the generated word should be capitalized.
 * @property activeDictionaries The list of dictionaries that are currently active.
 * @property decorator Settings that determine whether the output should be an array of values.
 */
data class WordScheme(
    var minLength: Int = DEFAULT_MIN_LENGTH,
    var maxLength: Int = DEFAULT_MAX_LENGTH,
    var enclosure: String = DEFAULT_ENCLOSURE,
    var capitalization: CapitalizationMode = DEFAULT_CAPITALIZATION,
    @get:XCollection(elementTypes = [BundledDictionary::class, UserDictionary::class])
    var activeDictionaries: MutableSet<Dictionary> = DEFAULT_ACTIVE_DICTIONARIES.toMutableSet(),
    override var decorator: ArraySchemeDecorator = ArraySchemeDecorator()
) : Scheme() {
    /**
     * Persistent storage of available dictionaries.
     */
    @Transient
    var dictionarySettings: Box<DictionarySettings> = Box({ DictionarySettings.default })

    @Transient
    override val name = "Word"

    override val icons = RandomnessIcons.Word


    /**
     * Returns random words from the dictionaries in `settings`.
     *
     * @param count the number of words to generate
     * @return random words from the dictionaries in `settings`
     * @throws InvalidDictionaryException if no words could be found using the settings in `settings`
     */
    override fun generateUndecoratedStrings(count: Int): List<String> {
        val words =
            activeDictionaries
                .flatMap { it.words }
                .filter { it.length in minLength..maxLength }
                .toSet()

        return List(count) { words.random(random) }
            .map { capitalization.transform(it) }
            .map { enclosure + it + enclosure }
    }

    override fun setSettingsState(settingsState: SettingsState) {
        super.setSettingsState(settingsState)
        dictionarySettings += settingsState.dictionarySettings
    }


    override fun doValidate(): String? {
        (+dictionarySettings).doValidate()?.also { return it }

        val words = activeDictionaries.flatMap { it.words }
        val minWordLength = words.map { it.length }.minOrNull() ?: 1
        val maxWordLength = words.map { it.length }.maxOrNull() ?: Integer.MAX_VALUE

        return when {
            minLength < MIN_LENGTH ->
                "Minimum length should not be smaller than $MIN_LENGTH."
            minLength > maxLength ->
                "Minimum length should not be larger than maximum length."
            activeDictionaries.isEmpty() ->
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
        copy(decorator = decorator.deepCopy(retainUuid))
            .also {
                if (retainUuid) it.uuid = this.uuid

                it.dictionarySettings = dictionarySettings.copy()
                it.activeDictionaries = activeDictionaries.map(Dictionary::deepCopy).toMutableSet()
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
         * The default value of the [minLength] field.
         */
        const val DEFAULT_MIN_LENGTH = 3

        /**
         * The default value of the [maxLength] field.
         */
        const val DEFAULT_MAX_LENGTH = 8

        /**
         * The default value of the [enclosure] field.
         */
        const val DEFAULT_ENCLOSURE = "\""

        /**
         * The default value of the [capitalization] field.
         */
        val DEFAULT_CAPITALIZATION = CapitalizationMode.RETAIN

        /**
         * The default value of the [activeDictionaries] field.
         */
        val DEFAULT_ACTIVE_DICTIONARIES: Set<Dictionary>
            get() = setOf(BundledDictionary(BundledDictionary.SIMPLE_DICTIONARY))
    }
}

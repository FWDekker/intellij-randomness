package com.fwdekker.randomness.word

import com.fwdekker.randomness.Box
import com.fwdekker.randomness.Bundle
import com.fwdekker.randomness.CapitalizationMode
import com.fwdekker.randomness.RandomnessIcons
import com.fwdekker.randomness.Scheme
import com.fwdekker.randomness.SchemeDecorator
import com.fwdekker.randomness.SettingsState
import com.fwdekker.randomness.State
import com.fwdekker.randomness.TypeIcon
import com.fwdekker.randomness.array.ArrayDecorator
import com.intellij.util.xmlb.annotations.Transient
import com.intellij.util.xmlb.annotations.XCollection
import java.awt.Color


/**
 * Contains settings for generating random words.
 *
 * @property minLength The minimum length of the generated word, inclusive.
 * @property maxLength The maximum length of the generated word, inclusive.
 * @property quotation The string that encloses the generated word on both sides.
 * @property customQuotation The quotation defined in the custom option.
 * @property capitalization The way in which the generated word should be capitalized.
 * @property activeDictionaries The list of dictionaries that are currently active.
 * @property arrayDecorator Settings that determine whether the output should be an array of values.
 */
data class WordScheme(
    var minLength: Int = DEFAULT_MIN_LENGTH,
    var maxLength: Int = DEFAULT_MAX_LENGTH,
    var quotation: String = DEFAULT_QUOTATION,
    var customQuotation: String = DEFAULT_CUSTOM_QUOTATION,
    var capitalization: CapitalizationMode = DEFAULT_CAPITALIZATION,
    @get:XCollection(elementTypes = [BundledDictionary::class, UserDictionary::class])
    var activeDictionaries: Set<Dictionary> = DEFAULT_ACTIVE_DICTIONARIES.toMutableSet(),
    var arrayDecorator: ArrayDecorator = ArrayDecorator()
) : Scheme() {
    /**
     * Persistent storage of available dictionaries.
     */
    @get:Transient
    var dictionarySettings: Box<DictionarySettings> = Box({ DictionarySettings.default })

    @get:Transient
    override val name = Bundle("word.title")
    override val typeIcon = BASE_ICON

    override val decorators: List<SchemeDecorator>
        get() = listOf(arrayDecorator)


    /**
     * Returns formatted random words from the dictionaries in [dictionarySettings].
     *
     * @param count the number of words to generate
     * @return formatted random words from the dictionaries in [dictionarySettings]
     * @throws InvalidDictionaryException if no words could be found using the settings in [dictionarySettings]
     */
    override fun generateUndecoratedStrings(count: Int): List<String> {
        val words =
            activeDictionaries
                .flatMap { it.words }
                .filter { it.length in minLength..maxLength }
                .toSet()

        return List(count) { words.random(random) }
            .map { capitalization.transform(it, random) }
            .map { inQuotes(it) }
    }

    /**
     * Encapsulates [string] in the quotes defined by [quotation].
     *
     * @param string the string to encapsulate
     * @return [string] encapsulated in the quotes defined by [quotation]
     */
    private fun inQuotes(string: String): String {
        val startQuote = quotation.getOrNull(0) ?: ""
        val endQuote = quotation.getOrNull(1) ?: startQuote

        return "$startQuote$string$endQuote"
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
            minLength < MIN_LENGTH -> Bundle("word.error.min_length_too_low", MIN_LENGTH)
            minLength > maxLength -> Bundle("word.error.min_length_above_max")
            activeDictionaries.isEmpty() -> Bundle("word.error.no_active_dictionary")
            minLength > maxWordLength -> Bundle("word.error.min_length_above_range", maxWordLength)
            maxLength < minWordLength -> Bundle("word.error.max_length_below_range", minWordLength)
            customQuotation.length > 2 -> Bundle("word.error.quotation_length")
            else -> arrayDecorator.doValidate()
        }
    }

    override fun copyFrom(other: State) {
        require(other is WordScheme) { Bundle("shared.error.cannot_copy_from_different_type") }

        (+dictionarySettings).also {
            it.copyFrom(+other.dictionarySettings)

            super.copyFrom(other)
            dictionarySettings += it
        }
    }

    override fun deepCopy(retainUuid: Boolean) =
        copy(
            activeDictionaries = activeDictionaries.map { it.deepCopy() }.toSet(),
            arrayDecorator = arrayDecorator.deepCopy(retainUuid)
        ).also {
            if (retainUuid) it.uuid = this.uuid

            it.dictionarySettings += (+dictionarySettings).deepCopy(retainUuid = retainUuid)
        }


    /**
     * Holds constants.
     */
    companion object {
        /**
         * The base icon for words.
         */
        val BASE_ICON = TypeIcon(RandomnessIcons.SCHEME, "cat", listOf(Color(242, 101, 34, 154)))

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
         * The default value of the [quotation] field.
         */
        const val DEFAULT_QUOTATION = "\""

        /**
         * The default value of the [customQuotation] field.
         */
        const val DEFAULT_CUSTOM_QUOTATION = "<>"

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

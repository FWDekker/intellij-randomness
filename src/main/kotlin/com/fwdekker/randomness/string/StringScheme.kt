package com.fwdekker.randomness.string

import com.fwdekker.randomness.CapitalizationMode
import com.fwdekker.randomness.DataGenerationException
import com.fwdekker.randomness.Scheme
import com.fwdekker.randomness.array.ArraySchemeDecorator
import com.intellij.util.xmlb.annotations.Transient


/**
 * Contains settings for generating random strings.
 *
 * @property symbolSetSettings Persistent storage of available symbol sets.
 * @property minLength The minimum length of the generated string, inclusive.
 * @property maxLength The maximum length of the generated string, inclusive.
 * @property enclosure The string that encloses the generated string on both sides.
 * @property capitalization The capitalization mode of the generated string.
 * @property activeSymbolSets The names of the symbol sets that are available for generating strings.
 * @property excludeLookAlikeSymbols Whether the symbols in [SymbolSet.lookAlikeCharacters] should be excluded.
 * @property decorator Settings that determine whether the output should be an array of values.
 */
data class StringScheme(
    @Transient
    var symbolSetSettings: SymbolSetSettings = SymbolSetSettings.default,
    var minLength: Int = DEFAULT_MIN_LENGTH,
    var maxLength: Int = DEFAULT_MAX_LENGTH,
    var enclosure: String = DEFAULT_ENCLOSURE,
    var capitalization: CapitalizationMode = DEFAULT_CAPITALIZATION,
    var activeSymbolSets: Set<String> = DEFAULT_ACTIVE_SYMBOL_SETS,
    var excludeLookAlikeSymbols: Boolean = DEFAULT_EXCLUDE_LOOK_ALIKE_SYMBOLS,
    override var decorator: ArraySchemeDecorator = ArraySchemeDecorator()
) : Scheme() {
    private val activeSymbols: List<String>
        get() =
            symbolSetSettings.symbolSetList
                .filter { it.name in activeSymbolSets }
                .sum(excludeLookAlikeSymbols)


    /**
     * Returns strings of random alphanumerical characters.
     *
     * @param count the number of strings to generate
     * @return strings of random alphanumerical characters
     */
    override fun generateUndecoratedStrings(count: Int): List<String> {
        doValidate()?.also { throw DataGenerationException(it) }

        val symbols = activeSymbols
        return List(count) {
            val length = random.nextInt(minLength, maxLength + 1)
            val text = List(length) { symbols.random(random) }.joinToString("")
            val capitalizedText = capitalization.transform(text)

            enclosure + capitalizedText + enclosure
        }
    }

    override fun doValidate(): String? {
        val allSymbolSets = symbolSetSettings.symbolSetList
        val duplicate = allSymbolSets.map { it.name }.firstNonDistinctOrNull()
        val empty = allSymbolSets.firstOrNull { it.symbols.isEmpty() }?.name
        val unknown = activeSymbolSets.firstOrNull { it !in allSymbolSets.map(SymbolSet::name) }

        return when {
            minLength < MIN_LENGTH ->
                "Minimum length should not be smaller than $MIN_LENGTH."
            minLength > maxLength ->
                "Minimum length should not be larger than maximum length."
            maxLength - minLength > MAX_LENGTH_DIFFERENCE ->
                "Value range should not exceed $MAX_LENGTH_DIFFERENCE."
            allSymbolSets.isEmpty() ->
                "Add at least one symbol set."
            allSymbolSets.any { it.name.isEmpty() } ->
                "All symbol sets should have a name."
            duplicate != null ->
                "There are multiple symbol sets with the name `$duplicate`."
            empty != null ->
                "Symbol set `$empty` should contain at least one symbol."
            unknown != null ->
                "Unknown symbol set `$unknown`."
            activeSymbolSets.isEmpty() ->
                "Activate at least one symbol set."
            activeSymbols.isEmpty() ->
                "Active symbol sets should contain at least one non-look-alike character if look-alike characters " +
                    "are excluded."
            else -> null
        }
    }


    override fun deepCopy() = copy().also { it.activeSymbolSets = activeSymbolSets.toSet() }


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
        const val MAX_LENGTH_DIFFERENCE = Int.MAX_VALUE.toDouble()

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
        val DEFAULT_CAPITALIZATION = CapitalizationMode.RANDOM

        /**
         * The default value of the [activeSymbolSets][activeSymbolSets] field.
         */
        val DEFAULT_ACTIVE_SYMBOL_SETS: Set<String>
            get() = listOf(SymbolSet.ALPHABET, SymbolSet.DIGITS).map { it.name }.toSet()

        /**
         * The default value of the [excludeLookAlikeSymbols][excludeLookAlikeSymbols] field.
         */
        const val DEFAULT_EXCLUDE_LOOK_ALIKE_SYMBOLS = false
    }
}

/**
 * Returns the first string that occurs multiple times, or `null` if there is no such string.
 *
 * @return the first string that occurs multiple times, or `null` if there is no such string
 */
private fun List<String>.firstNonDistinctOrNull() = firstOrNull { indexOf(it) != lastIndexOf(it) }

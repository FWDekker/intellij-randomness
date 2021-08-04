package com.fwdekker.randomness.string

import com.fwdekker.randomness.CapitalizationMode
import com.fwdekker.randomness.DataGenerationException
import com.fwdekker.randomness.Scheme


/**
 * Contains settings for generating random strings.
 *
 * @property minLength The minimum length of the generated string, inclusive.
 * @property maxLength The maximum length of the generated string, inclusive.
 * @property enclosure The string that encloses the generated string on both sides.
 * @property capitalization The capitalization mode of the generated string.
 * @property symbolSets The symbol sets that are available for generating strings.
 * @property activeSymbolSets The symbol sets that are actually used for generating strings; a subset of
 * [symbolSets].
 * @property excludeLookAlikeSymbols Whether the symbols in [SymbolSet.lookAlikeCharacters] should be excluded.
 */
// TODO: Allow separate definition of symbol sets
data class StringScheme(
    var minLength: Int = DEFAULT_MIN_LENGTH,
    var maxLength: Int = DEFAULT_MAX_LENGTH,
    var enclosure: String = DEFAULT_ENCLOSURE,
    var capitalization: CapitalizationMode = DEFAULT_CAPITALIZATION,
    var symbolSets: Set<SymbolSet> = DEFAULT_SYMBOL_SETS,
    var activeSymbolSets: Set<SymbolSet> = DEFAULT_ACTIVE_SYMBOL_SETS,
    var excludeLookAlikeSymbols: Boolean = DEFAULT_EXCLUDE_LOOK_ALIKE_SYMBOLS
) : Scheme<StringScheme>() {
    /**
     * Returns strings of random alphanumerical characters.
     *
     * @param count the number of strings to generate
     * @return strings of random alphanumerical characters
     */
    override fun generateStrings(count: Int): List<String> {
        if (minLength > maxLength)
            throw DataGenerationException("Minimum length is larger than maximum length.")

        val symbols = activeSymbolSets.sum(excludeLookAlikeSymbols)
        if (symbols.isEmpty())
            throw DataGenerationException("No valid symbols found in active symbol sets.")

        return List(count) {
            val length = random.nextInt(minLength, maxLength + 1)
            val text = List(length) { symbols.random(random) }.joinToString("")
            val capitalizedText = capitalization.transform(text)

            enclosure + capitalizedText + enclosure
        }
    }


    override fun deepCopy() =
        StringScheme(
            minLength, maxLength, enclosure, capitalization,
            symbolSets.map { SymbolSet(it.name, it.symbols) }.toSet(),
            activeSymbolSets.map { SymbolSet(it.name, it.symbols) }.toSet()
        )


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
        val DEFAULT_CAPITALIZATION = CapitalizationMode.RANDOM

        /**
         * The default value of the [symbolSets][symbolSets] field.
         */
        val DEFAULT_SYMBOL_SETS = SymbolSet.defaultSymbolSets.toSet()

        /**
         * The default value of the [activeSymbolSets][activeSymbolSets] field.
         */
        val DEFAULT_ACTIVE_SYMBOL_SETS = listOf(SymbolSet.ALPHABET, SymbolSet.DIGITS).toSet()

        /**
         * The default value of the [excludeLookAlikeSymbols][excludeLookAlikeSymbols] field.
         */
        const val DEFAULT_EXCLUDE_LOOK_ALIKE_SYMBOLS = false
    }
}

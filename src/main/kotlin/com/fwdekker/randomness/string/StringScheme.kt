package com.fwdekker.randomness.string

import com.fwdekker.randomness.CapitalizationMode
import com.fwdekker.randomness.DataGenerationException
import com.fwdekker.randomness.Scheme
import com.intellij.util.xmlb.XmlSerializerUtil
import com.intellij.util.xmlb.annotations.MapAnnotation
import com.intellij.util.xmlb.annotations.Transient
import com.vdurmont.emoji.EmojiParser
import kotlin.random.Random


/**
 * Contains settings for generating random strings.
 *
 * @property minLength The minimum length of the generated string, inclusive.
 * @property maxLength The maximum length of the generated string, inclusive.
 * @property enclosure The string that encloses the generated string on both sides.
 * @property capitalization The capitalization mode of the generated string.
 * @property serializedSymbolSets The symbol sets that are available for generating strings. Emoji have been serialized
 * for compatibility with JetBrains' serializer.
 * @property serializedActiveSymbolSets The symbol sets that are actually used for generating strings; a subset of
 * [symbolSets]. Emoji have been serialized for compatibility with JetBrains' serializer.
 * @property excludeLookAlikeSymbols Whether the symbols in [SymbolSet.lookAlikeCharacters] should be excluded.
 */
data class StringScheme(
    var minLength: Int = DEFAULT_MIN_LENGTH,
    var maxLength: Int = DEFAULT_MAX_LENGTH,
    var enclosure: String = DEFAULT_ENCLOSURE,
    var capitalization: CapitalizationMode = DEFAULT_CAPITALIZATION,
    @MapAnnotation(sortBeforeSave = false)
    var serializedSymbolSets: Map<String, String> = DEFAULT_SYMBOL_SETS.toMap(),
    @MapAnnotation(sortBeforeSave = false)
    var serializedActiveSymbolSets: Map<String, String> = DEFAULT_ACTIVE_SYMBOL_SETS.toMap(),
    var excludeLookAlikeSymbols: Boolean = DEFAULT_EXCLUDE_LOOK_ALIKE_SYMBOLS
) : Scheme<StringScheme> {
    private val random: Random = Random.Default


    /**
     * Same as [symbolSets], except that serialized emoji have been deserialized.
     */
    var symbolSets: Map<String, String>
        @Transient
        get() = serializedSymbolSets.map { SymbolSet(it.key, EmojiParser.parseToUnicode(it.value)) }.toMap()
        set(value) {
            serializedSymbolSets = value.map { SymbolSet(it.key, EmojiParser.parseToAliases(it.value)) }.toMap()
        }

    /**
     * Same as [activeSymbolSets], except that serialized emoji have been deserialized.
     */
    var activeSymbolSets: Map<String, String>
        @Transient
        get() = serializedActiveSymbolSets.map { SymbolSet(it.key, EmojiParser.parseToUnicode(it.value)) }.toMap()
        set(value) {
            serializedActiveSymbolSets = value.map { SymbolSet(it.key, EmojiParser.parseToAliases(it.value)) }.toMap()
        }

    /**
     * A list view of the `SymbolSet` objects described by [symbolSets].
     */
    var symbolSetList: Collection<SymbolSet>
        @Transient
        get() = symbolSets.toSymbolSets()
        set(value) {
            symbolSets = value.toMap()
        }

    /**
     * A list view of the `SymbolSet` objects described by [activeSymbolSets].
     */
    var activeSymbolSetList: Collection<SymbolSet>
        @Transient
        get() = activeSymbolSets.toSymbolSets()
        set(value) {
            activeSymbolSets = value.toMap()
        }


    /**
     * Returns strings of random alphanumerical characters.
     *
     * @param count the number of strings to generate
     * @return strings of random alphanumerical characters
     */
    override fun generateStrings(count: Int): List<String> {
        if (minLength > maxLength)
            throw DataGenerationException("Minimum length is larger than maximum length.")

        val symbols = activeSymbolSetList.sum(excludeLookAlikeSymbols)
        if (symbols.isEmpty())
            throw DataGenerationException("No valid symbols found in active symbol sets.")

        return List(count) {
            val length = random.nextInt(minLength, maxLength + 1)
            val text = List(length) { symbols.random(random) }.joinToString("")
            val capitalizedText = capitalization.transform(text)

            enclosure + capitalizedText + enclosure
        }
    }


    override fun copyFrom(other: StringScheme) = XmlSerializerUtil.copyBean(other, this)


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
        val DEFAULT_SYMBOL_SETS = SymbolSet.defaultSymbolSets.toMap()

        /**
         * The default value of the [activeSymbolSets][activeSymbolSets] field.
         */
        val DEFAULT_ACTIVE_SYMBOL_SETS = listOf(SymbolSet.ALPHABET, SymbolSet.DIGITS).toMap()

        /**
         * The default value of the [excludeLookAlikeSymbols][excludeLookAlikeSymbols] field.
         */
        const val DEFAULT_EXCLUDE_LOOK_ALIKE_SYMBOLS = false
    }
}

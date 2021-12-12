package com.fwdekker.randomness.word

import com.fwdekker.randomness.Bundle
import com.fwdekker.randomness.CapitalizationMode
import com.fwdekker.randomness.RandomnessIcons
import com.fwdekker.randomness.Scheme
import com.fwdekker.randomness.SchemeDecorator
import com.fwdekker.randomness.TypeIcon
import com.fwdekker.randomness.array.ArrayDecorator
import com.intellij.util.xmlb.annotations.Transient
import java.awt.Color


/**
 * Contains settings for generating random words.
 *
 * @property quotation The string that encloses the generated word on both sides.
 * @property customQuotation The quotation defined in the custom option.
 * @property capitalization The way in which the generated word should be capitalized.
 * @property words The list of words to choose from.
 * @property arrayDecorator Settings that determine whether the output should be an array of values.
 */
data class WordScheme(
    var quotation: String = DEFAULT_QUOTATION,
    var customQuotation: String = DEFAULT_CUSTOM_QUOTATION,
    var capitalization: CapitalizationMode = DEFAULT_CAPITALIZATION,
    var words: List<String> = DEFAULT_WORDS,
    var arrayDecorator: ArrayDecorator = ArrayDecorator()
) : Scheme() {
    @get:Transient
    override val name = Bundle("word.title")
    override val typeIcon = BASE_ICON

    override val decorators: List<SchemeDecorator>
        get() = listOf(arrayDecorator)


    /**
     * Returns formatted random words.
     *
     * @param count the number of words to generate
     * @return formatted random words
     */
    override fun generateUndecoratedStrings(count: Int) =
        List(count) { words.random(random) }
            .map { capitalization.transform(it, random) }
            .map { inQuotes(it) }

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


    override fun doValidate(): String? {
        return when {
            customQuotation.length > 2 -> Bundle("word.error.quotation_length")
            words.isEmpty() -> Bundle("word.error.empty_word_list")
            else -> arrayDecorator.doValidate()
        }
    }

    override fun deepCopy(retainUuid: Boolean) =
        copy(arrayDecorator = arrayDecorator.deepCopy(retainUuid))
            .also { if (retainUuid) it.uuid = this.uuid }


    /**
     * Holds constants.
     */
    companion object {
        /**
         * The base icon for words.
         */
        val BASE_ICON = TypeIcon(RandomnessIcons.SCHEME, "cat", listOf(Color(242, 101, 34, 154)))

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
         * The default value of the [words] field.
         */
        val DEFAULT_WORDS: List<String>
            get() = listOf("lorem", "ipsum", "dolor", "sit", "amet")
    }
}

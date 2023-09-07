package com.fwdekker.randomness.word

import com.fwdekker.randomness.Bundle
import com.fwdekker.randomness.CapitalizationMode
import com.fwdekker.randomness.Icons
import com.fwdekker.randomness.Scheme
import com.fwdekker.randomness.TypeIcon
import com.fwdekker.randomness.affix.AffixDecorator
import com.fwdekker.randomness.array.ArrayDecorator
import com.intellij.ui.JBColor
import java.awt.Color


/**
 * Contains settings for generating random words.
 *
 * @property words The list of words to choose from.
 * @property capitalization The way in which the generated word should be capitalized.
 * @property affixDecorator The affixation to apply to the generated values.
 * @property arrayDecorator Settings that determine whether the output should be an array of values.
 */
data class WordScheme(
    var words: List<String> = DEFAULT_WORDS,
    var capitalization: CapitalizationMode = DEFAULT_CAPITALIZATION,
    val affixDecorator: AffixDecorator = DEFAULT_AFFIX_DECORATOR,
    val arrayDecorator: ArrayDecorator = DEFAULT_ARRAY_DECORATOR,
) : Scheme() {
    override val name = Bundle("word.title")
    override val typeIcon = BASE_ICON
    override val decorators get() = listOf(affixDecorator, arrayDecorator)


    /**
     * Returns formatted random words.
     *
     * @param count the number of words to generate
     * @return formatted random words
     */
    override fun generateUndecoratedStrings(count: Int) =
        List(count) { capitalization.transform(words.random(random), random) }


    override fun doValidate() =
        if (words.isEmpty()) Bundle("word.error.empty_word_list")
        else affixDecorator.doValidate() ?: arrayDecorator.doValidate()

    override fun deepCopy(retainUuid: Boolean) =
        copy(
            words = words.toList(),
            affixDecorator = affixDecorator.deepCopy(retainUuid),
            arrayDecorator = arrayDecorator.deepCopy(retainUuid),
        ).deepCopyTransient(retainUuid)


    /**
     * Holds constants.
     */
    companion object {
        /**
         * The base icon for words.
         */
        val BASE_ICON = TypeIcon(
            Icons.SCHEME,
            "cat",
            listOf(JBColor(Color(242, 101, 34, 154), Color(242, 101, 34, 154)))
        )

        /**
         * The default value of the [words] field.
         */
        val DEFAULT_WORDS get() = listOf("lorem", "ipsum", "dolor", "sit", "amet")

        /**
         * The preset values for the [capitalization] field.
         */
        val PRESET_CAPITALIZATION = arrayOf(
            CapitalizationMode.RETAIN,
            CapitalizationMode.LOWER,
            CapitalizationMode.UPPER,
            CapitalizationMode.RANDOM,
            CapitalizationMode.SENTENCE,
            CapitalizationMode.FIRST_LETTER,
        )

        /**
         * The default value of the [capitalization] field.
         */
        val DEFAULT_CAPITALIZATION = CapitalizationMode.RETAIN

        /**
         * The preset values for the [affixDecorator] descriptor.
         */
        val PRESET_AFFIX_DECORATOR_DESCRIPTORS = listOf("'", "\"", "`")

        /**
         * The default value of the [affixDecorator] field.
         */
        val DEFAULT_AFFIX_DECORATOR get() = AffixDecorator(enabled = false, descriptor = "\"")

        /**
         * The default value of the [arrayDecorator] field.
         */
        val DEFAULT_ARRAY_DECORATOR get() = ArrayDecorator()
    }
}

package com.fwdekker.randomness.string

import com.fwdekker.randomness.Bundle
import com.fwdekker.randomness.CapitalizationMode
import com.fwdekker.randomness.RandomnessIcons
import com.fwdekker.randomness.Scheme
import com.fwdekker.randomness.SchemeDecorator
import com.fwdekker.randomness.TypeIcon
import com.fwdekker.randomness.array.ArrayDecorator
import com.fwdekker.randomness.string.StringScheme.Companion.LOOK_ALIKE_CHARACTERS
import com.github.curiousoddman.rgxgen.RgxGen
import com.github.curiousoddman.rgxgen.parsing.dflt.RgxGenParseException
import com.intellij.util.xmlb.annotations.Transient
import java.awt.Color
import kotlin.random.asJavaRandom


/**
 * Contains settings for generating random strings.
 *
 * @property pattern The regex-like pattern according to which the string is generated.
 * @property capitalization The capitalization mode of the generated string.
 * @property removeLookAlikeSymbols Whether the symbols in [LOOK_ALIKE_CHARACTERS] should be removed.
 * @property arrayDecorator Settings that determine whether the output should be an array of values.
 */
data class StringScheme(
    var pattern: String = DEFAULT_PATTERN,
    var capitalization: CapitalizationMode = DEFAULT_CAPITALIZATION,
    var removeLookAlikeSymbols: Boolean = DEFAULT_REMOVE_LOOK_ALIKE_SYMBOLS,
    var arrayDecorator: ArrayDecorator = ArrayDecorator()
) : Scheme() {
    @get:Transient
    override val name = Bundle("string.title")
    override val typeIcon = BASE_ICON

    override val decorators: List<SchemeDecorator>
        get() = listOf(arrayDecorator)


    /**
     * Returns strings of random alphanumerical characters.
     *
     * @param count the number of strings to generate
     * @return strings of random alphanumerical characters
     */
    override fun generateUndecoratedStrings(count: Int): List<String> {
        val rgxGen = RgxGen(pattern)
        return List(count) {
            val text = rgxGen.generate(random.asJavaRandom())
            val capitalizedText = capitalization.transform(text, random)

            if (removeLookAlikeSymbols) capitalizedText.filterNot { it in LOOK_ALIKE_CHARACTERS }
            else capitalizedText
        }
    }


    override fun doValidate() =
        try {
            RgxGen(pattern)
            arrayDecorator.doValidate()
        } catch (e: RgxGenParseException) {
            e.message
        }

    override fun deepCopy(retainUuid: Boolean) =
        StringScheme(
            pattern = pattern,
            capitalization = capitalization,
            removeLookAlikeSymbols = removeLookAlikeSymbols,
            arrayDecorator = arrayDecorator.deepCopy(retainUuid)
        ).also { if (retainUuid) it.uuid = this.uuid }


    /**
     * Holds constants.
     */
    companion object {
        /**
         * Symbols that look like other symbols.
         *
         * To be precise, this string contains the symbols `0`, `1`, `l`, `I`, `O`, `|`, and `﹒`.
         */
        const val LOOK_ALIKE_CHARACTERS = "01lLiIoO|﹒"

        /**
         * The base icon for strings.
         */
        val BASE_ICON = TypeIcon(RandomnessIcons.SCHEME, "abc", listOf(Color(244, 175, 61, 154)))

        /**
         * The default value of the [pattern] field.
         */
        const val DEFAULT_PATTERN = "[a-z0-9]{8}"

        /**
         * The default value of the [capitalization] field.
         */
        val DEFAULT_CAPITALIZATION = CapitalizationMode.RETAIN

        /**
         * The default value of the [removeLookAlikeSymbols] field.
         */
        const val DEFAULT_REMOVE_LOOK_ALIKE_SYMBOLS = false
    }
}

package com.fwdekker.randomness.string

import com.fwdekker.randomness.Bundle
import com.fwdekker.randomness.CapitalizationMode
import com.fwdekker.randomness.Icons
import com.fwdekker.randomness.Scheme
import com.fwdekker.randomness.TypeIcon
import com.fwdekker.randomness.array.ArrayDecorator
import com.fwdekker.randomness.string.StringScheme.Companion.LOOK_ALIKE_CHARACTERS
import com.github.curiousoddman.rgxgen.RgxGen
import com.github.curiousoddman.rgxgen.parsing.dflt.RgxGenParseException
import com.intellij.ui.JBColor
import java.awt.Color
import kotlin.random.asJavaRandom


/**
 * Contains settings for generating random strings.
 *
 * @property pattern The regex-like pattern according to which the string is generated.
 * @property isRegex `true` if and only if [pattern] should be interpreted as a regex.
 * @property removeLookAlikeSymbols Whether the symbols in [LOOK_ALIKE_CHARACTERS] should be removed.
 * @property capitalization The capitalization mode of the generated string.
 * @property arrayDecorator Settings that determine whether the output should be an array of values.
 */
data class StringScheme(
    var pattern: String = DEFAULT_PATTERN,
    var isRegex: Boolean = DEFAULT_IS_REGEX,
    var removeLookAlikeSymbols: Boolean = DEFAULT_REMOVE_LOOK_ALIKE_SYMBOLS,
    var capitalization: CapitalizationMode = DEFAULT_CAPITALIZATION,
    var arrayDecorator: ArrayDecorator = ArrayDecorator(),
) : Scheme() {
    override val name = Bundle("string.title")
    override val typeIcon = BASE_ICON
    override val decorators get() = listOf(arrayDecorator)


    /**
     * Returns strings of random alphanumerical characters.
     *
     * @param count the number of strings to generate
     * @return strings of random alphanumerical characters
     */
    override fun generateUndecoratedStrings(count: Int): List<String> {
        val rawStrings =
            if (isRegex) {
                val rgxGen = RgxGen(pattern)
                List(count) { rgxGen.generate(random.asJavaRandom()) }
            } else {
                List(count) { pattern }
            }

        return rawStrings.map { rawString ->
            val capitalizedString = capitalization.transform(rawString, random)

            if (removeLookAlikeSymbols) capitalizedString.filterNot { it in LOOK_ALIKE_CHARACTERS }
            else capitalizedString
        }
    }


    /**
     * Returns `true` if and only if this scheme does not use any regex functionality beyond escape characters.
     *
     * @return `true` if and only if this scheme does not use any regex functionality beyond escape characters
     */
    fun isSimple() =
        doValidate() == null &&
            generateStrings()[0] == if (isRegex) pattern.replace(Regex("\\\\(.)"), "$1") else pattern


    override fun doValidate() =
        when {
            !isRegex -> arrayDecorator.doValidate()
            pattern.takeLastWhile { it == '\\' }.length.mod(2) != 0 -> Bundle("string.error.trailing_backslash")
            pattern == "{}" || pattern.contains(Regex("""[^\\]\{}""")) -> Bundle("string.error.empty_curly")
            pattern == "[]" || pattern.contains(Regex("""[^\\]\[]""")) -> Bundle("string.error.empty_square")
            else ->
                @Suppress("detekt:TooGenericExceptionCaught") // Consequence of incomplete validation in RgxGen
                try {
                    RgxGen(pattern).generate()
                    arrayDecorator.doValidate()
                } catch (e: RgxGenParseException) {
                    e.message
                } catch (e: Exception) {
                    "Uncaught RgxGen exception: ${e.message}"
                }
        }

    override fun deepCopy(retainUuid: Boolean) =
        copy(arrayDecorator = arrayDecorator.deepCopy(retainUuid)).deepCopyTransient(retainUuid)


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
        val BASE_ICON = TypeIcon(
            Icons.SCHEME,
            "abc",
            listOf(JBColor(Color(244, 175, 61, 154), Color(244, 175, 61, 154)))
        )

        /**
         * The default value of the [pattern] field.
         */
        const val DEFAULT_PATTERN = "[a-zA-Z0-9]{7,11}"

        /**
         * The default value of the [isRegex] field.
         */
        const val DEFAULT_IS_REGEX = true

        /**
         * The default value of the [removeLookAlikeSymbols] field.
         */
        const val DEFAULT_REMOVE_LOOK_ALIKE_SYMBOLS = false

        /**
         * The preset values for the [capitalization] field.
         */
        val PRESET_CAPITALIZATION = arrayOf(
            CapitalizationMode.RETAIN,
            CapitalizationMode.LOWER,
            CapitalizationMode.UPPER,
            CapitalizationMode.RANDOM,
        )

        /**
         * The default value of the [capitalization] field.
         */
        val DEFAULT_CAPITALIZATION = CapitalizationMode.RETAIN
    }
}

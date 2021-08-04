package com.fwdekker.randomness.decimal

import com.fwdekker.randomness.DataGenerationException
import com.fwdekker.randomness.Scheme
import java.text.DecimalFormat
import kotlin.math.nextUp


/**
 * Contains settings for generating random decimals.
 *
 * @property minValue The minimum value to be generated, inclusive.
 * @property maxValue The maximum value to be generated, inclusive.
 * @property decimalCount The number of decimals to display.
 * @property showTrailingZeroes Whether to include trailing zeroes in the decimals.
 * @property groupingSeparator The character that should separate groups.
 * @property decimalSeparator The character that should separate decimals.
 * @property prefix The string to prepend to the generated value.
 * @property suffix The string to append to the generated value.
 */
data class DecimalScheme(
    var minValue: Double = DEFAULT_MIN_VALUE,
    var maxValue: Double = DEFAULT_MAX_VALUE,
    var decimalCount: Int = DEFAULT_DECIMAL_COUNT,
    var showTrailingZeroes: Boolean = DEFAULT_SHOW_TRAILING_ZEROES,
    var groupingSeparator: String = DEFAULT_GROUPING_SEPARATOR,
    var decimalSeparator: String = DEFAULT_DECIMAL_SEPARATOR,
    var prefix: String = DEFAULT_PREFIX,
    var suffix: String = DEFAULT_SUFFIX
) : Scheme<DecimalScheme>() {
    override val descriptor
        get() = "%Dec[" +
            "minValue=$minValue, " +
            "maxValue=$maxValue, " +
            "decimalCount=$decimalCount, " +
            "showTrailingZeroes=$showTrailingZeroes, " +
            "groupingSeparator=$groupingSeparator, " +
            "decimalSeparator=$decimalSeparator, " +
            "prefix=$prefix, " +
            "suffix=$suffix" +
            "]"


    /**
     * Returns random decimals between the minimum and maximum value, inclusive.
     *
     * @param count the number of decimals to generate
     * @return random decimals between the minimum and maximum value, inclusive
     */
    override fun generateStrings(count: Int): List<String> {
        if (minValue > maxValue)
            throw DataGenerationException("Minimum value is larger than maximum value.")

        return List(count) { doubleToString(random.nextDouble(minValue, maxValue.nextUp())) }
    }

    /**
     * Returns a nicely formatted representation of a decimal.
     *
     * @param decimal the decimal to format
     * @return a nicely formatted representation of a decimal
     */
    private fun doubleToString(decimal: Double): String {
        val format = DecimalFormat()
        format.isGroupingUsed = groupingSeparator.isNotEmpty()

        val symbols = format.decimalFormatSymbols
        symbols.groupingSeparator = groupingSeparator.getOrElse(0) { Char.MIN_VALUE }
        symbols.decimalSeparator = decimalSeparator.getOrElse(0) { Char.MIN_VALUE }
        if (showTrailingZeroes) format.minimumFractionDigits = decimalCount
        format.maximumFractionDigits = decimalCount
        format.decimalFormatSymbols = symbols

        return prefix + format.format(decimal) + suffix
    }


    /**
     * Holds constants.
     */
    companion object {
        /**
         * The default value of the [minValue][minValue] field.
         */
        const val DEFAULT_MIN_VALUE = 0.0

        /**
         * The default value of the [maxValue][maxValue] field.
         */
        const val DEFAULT_MAX_VALUE = 1_000.0

        /**
         * The default value of the [decimalCount][decimalCount] field.
         */
        const val DEFAULT_DECIMAL_COUNT = 2

        /**
         * The default value of the [showTrailingZeroes][showTrailingZeroes] field.
         */
        const val DEFAULT_SHOW_TRAILING_ZEROES = true

        /**
         * The default value of the [groupingSeparator][groupingSeparator] field.
         */
        const val DEFAULT_GROUPING_SEPARATOR = ""

        /**
         * The default value of the [decimalSeparator][decimalSeparator] field.
         */
        const val DEFAULT_DECIMAL_SEPARATOR = "."

        /**
         * The default value of the [prefix][prefix] field.
         */
        const val DEFAULT_PREFIX = ""

        /**
         * The default value of the [suffix][suffix] field.
         */
        const val DEFAULT_SUFFIX = ""
    }
}

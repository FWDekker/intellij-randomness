package com.fwdekker.randomness.decimal

import com.fwdekker.randomness.DataGenerationException
import com.fwdekker.randomness.Scheme
import com.fwdekker.randomness.array.ArraySchemeDecorator
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
 * @property decorator Settings that determine whether the output should be an array of values.
 */
data class DecimalScheme(
    var minValue: Double = DEFAULT_MIN_VALUE,
    var maxValue: Double = DEFAULT_MAX_VALUE,
    var decimalCount: Int = DEFAULT_DECIMAL_COUNT,
    var showTrailingZeroes: Boolean = DEFAULT_SHOW_TRAILING_ZEROES,
    var groupingSeparator: String = DEFAULT_GROUPING_SEPARATOR,
    var decimalSeparator: String = DEFAULT_DECIMAL_SEPARATOR,
    var prefix: String = DEFAULT_PREFIX,
    var suffix: String = DEFAULT_SUFFIX,
    override var decorator: ArraySchemeDecorator = ArraySchemeDecorator()
) : Scheme() {
    /**
     * Returns random decimals between the minimum and maximum value, inclusive.
     *
     * @param count the number of decimals to generate
     * @return random decimals between the minimum and maximum value, inclusive
     */
    override fun generateUndecoratedStrings(count: Int): List<String> {
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
        doValidate()?.also { throw DataGenerationException(it) }

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

    override fun doValidate() =
        when {
            minValue > maxValue -> "Minimum value should not be larger than maximum value."
            maxValue - minValue > MAX_VALUE_DIFFERENCE -> "Value range should not exceed $MAX_VALUE_DIFFERENCE."
            decimalCount < MIN_DECIMAL_COUNT -> "Decimal count should be at least $MIN_DECIMAL_COUNT."
            else -> null
        }


    override fun deepCopy() = copy(decorator = decorator.deepCopy())


    /**
     * Holds constants.
     */
    companion object {
        /**
         * The maximum valid difference between the [minValue] and [maxValue] fields.
         */
        const val MAX_VALUE_DIFFERENCE = 1E53

        /**
         * The default value of the [minValue][minValue] field.
         */
        const val DEFAULT_MIN_VALUE = 0.0

        /**
         * The default value of the [maxValue][maxValue] field.
         */
        const val DEFAULT_MAX_VALUE = 1_000.0

        /**
         * The minimum valid value for the [decimalCount] field.
         */
        const val MIN_DECIMAL_COUNT = 0

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

package com.fwdekker.randomness.decimal

import com.fwdekker.randomness.Bundle
import com.fwdekker.randomness.RandomnessIcons
import com.fwdekker.randomness.Scheme
import com.fwdekker.randomness.SchemeDecorator
import com.fwdekker.randomness.TypeIcon
import com.fwdekker.randomness.array.ArrayDecorator
import com.intellij.util.xmlb.annotations.Transient
import java.awt.Color
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
 * @property customGroupingSeparator The grouping separator defined in the custom option.
 * @property decimalSeparator The character that should separate decimals.
 * @property customDecimalSeparator The decimal separator defined in the custom option.
 * @property prefix The string to prepend to the generated value.
 * @property suffix The string to append to the generated value.
 * @property arrayDecorator Settings that determine whether the output should be an array of values.
 */
data class DecimalScheme(
    var minValue: Double = DEFAULT_MIN_VALUE,
    var maxValue: Double = DEFAULT_MAX_VALUE,
    var decimalCount: Int = DEFAULT_DECIMAL_COUNT,
    var showTrailingZeroes: Boolean = DEFAULT_SHOW_TRAILING_ZEROES,
    var groupingSeparator: String = DEFAULT_GROUPING_SEPARATOR,
    var customGroupingSeparator: String = DEFAULT_CUSTOM_GROUPING_SEPARATOR,
    var decimalSeparator: String = DEFAULT_DECIMAL_SEPARATOR,
    var customDecimalSeparator: String = DEFAULT_CUSTOM_DECIMAL_SEPARATOR,
    var prefix: String = DEFAULT_PREFIX,
    var suffix: String = DEFAULT_SUFFIX,
    var arrayDecorator: ArrayDecorator = ArrayDecorator()
) : Scheme() {
    @get:Transient
    override val name = Bundle("decimal.title")
    override val typeIcon = BASE_ICON

    override val decorators: List<SchemeDecorator>
        get() = listOf(arrayDecorator)


    /**
     * Returns random formatted decimals in the range from [minValue] until [maxValue], inclusive.
     *
     * @param count the number of decimals to generate
     * @return random formatted decimals in the range from [minValue] until [maxValue], inclusive
     */
    override fun generateUndecoratedStrings(count: Int) =
        List(count) { doubleToString(random.nextDouble(minValue, maxValue.nextUp())) }

    /**
     * Returns a nicely formatted representation of [decimal].
     *
     * @param decimal the decimal to format
     * @return a nicely formatted representation of [decimal]
     */
    private fun doubleToString(decimal: Double): String {
        val format = DecimalFormat()
        format.isGroupingUsed = groupingSeparator.isNotEmpty()

        if (showTrailingZeroes) format.minimumFractionDigits = decimalCount
        format.isGroupingUsed = groupingSeparator.isNotEmpty()
        format.maximumFractionDigits = decimalCount
        format.decimalFormatSymbols = format.decimalFormatSymbols
            .also {
                it.groupingSeparator = groupingSeparator.getOrElse(0) { Char.MIN_VALUE }
                it.decimalSeparator = decimalSeparator[0]
            }

        return prefix + format.format(decimal) + suffix
    }


    override fun doValidate() =
        when {
            minValue > maxValue -> Bundle("decimal.error.min_value_above_max")
            maxValue - minValue > MAX_VALUE_DIFFERENCE -> Bundle("decimal.error.value_range", MAX_VALUE_DIFFERENCE)
            decimalCount < MIN_DECIMAL_COUNT -> Bundle("decimal.error.decimal_count_too_low", MIN_DECIMAL_COUNT)
            groupingSeparator.length > 1 -> Bundle("decimal.error.grouping_separator_length")
            decimalSeparator.length != 1 -> Bundle("decimal.error.decimal_separator_length")
            else -> arrayDecorator.doValidate()
        }

    override fun deepCopy(retainUuid: Boolean) =
        copy(arrayDecorator = arrayDecorator.deepCopy(retainUuid))
            .also { if (retainUuid) it.uuid = this.uuid }


    /**
     * Holds constants.
     */
    companion object {
        /**
         * The base icon for decimals.
         */
        val BASE_ICON = TypeIcon(RandomnessIcons.SCHEME, "4.2", listOf(Color(98, 181, 67, 154)))

        /**
         * The maximum valid difference between the [minValue] and [maxValue] fields.
         */
        const val MAX_VALUE_DIFFERENCE = 1E53

        /**
         * The default value of the [minValue] field.
         */
        const val DEFAULT_MIN_VALUE = 0.0

        /**
         * The default value of the [maxValue] field.
         */
        const val DEFAULT_MAX_VALUE = 1_000.0

        /**
         * The minimum valid value for the [decimalCount] field.
         */
        const val MIN_DECIMAL_COUNT = 0

        /**
         * The default value of the [decimalCount] field.
         */
        const val DEFAULT_DECIMAL_COUNT = 2

        /**
         * The default value of the [showTrailingZeroes] field.
         */
        const val DEFAULT_SHOW_TRAILING_ZEROES = true

        /**
         * The default value of the [groupingSeparator] field.
         */
        const val DEFAULT_GROUPING_SEPARATOR = ""

        /**
         * The default value of the [customGroupingSeparator] field.
         */
        const val DEFAULT_CUSTOM_GROUPING_SEPARATOR = "'"

        /**
         * The default value of the [decimalSeparator] field.
         */
        const val DEFAULT_DECIMAL_SEPARATOR = "."

        /**
         * The default value of the [customDecimalSeparator] field.
         */
        const val DEFAULT_CUSTOM_DECIMAL_SEPARATOR = "/"

        /**
         * The default value of the [prefix] field.
         */
        const val DEFAULT_PREFIX = ""

        /**
         * The default value of the [suffix] field.
         */
        const val DEFAULT_SUFFIX = ""
    }
}

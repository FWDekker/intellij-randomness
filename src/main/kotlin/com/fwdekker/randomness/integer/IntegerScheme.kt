package com.fwdekker.randomness.integer

import com.fwdekker.randomness.Bundle
import com.fwdekker.randomness.CapitalizationMode
import com.fwdekker.randomness.RandomnessIcons
import com.fwdekker.randomness.Scheme
import com.fwdekker.randomness.SchemeDecorator
import com.fwdekker.randomness.TypeIcon
import com.fwdekker.randomness.array.ArrayDecorator
import com.fwdekker.randomness.fixedlength.FixedLengthDecorator
import com.intellij.util.xmlb.annotations.Transient
import java.awt.Color
import java.text.DecimalFormat


/**
 * Contains settings for generating random integers.
 *
 * @property minValue The minimum value to be generated, inclusive.
 * @property maxValue The maximum value to be generated, inclusive.
 * @property base The base the generated value should be displayed in.
 * @property groupingSeparator The character that should separate groups.
 * @property customGroupingSeparator The grouping separator defined in the custom option.
 * @property capitalization The capitalization mode of the generated integer, applicable for bases higher than 10.
 * @property prefix The string to prepend to the generated value.
 * @property suffix The string to append to the generated value.
 * @property fixedLengthDecorator Settings that determine whether the output should be fixed to a specific length.
 * @property arrayDecorator Settings that determine whether the output should be an array of values.
 */
data class IntegerScheme(
    var minValue: Long = DEFAULT_MIN_VALUE,
    var maxValue: Long = DEFAULT_MAX_VALUE,
    var base: Int = DEFAULT_BASE,
    var groupingSeparator: String = DEFAULT_GROUPING_SEPARATOR,
    var customGroupingSeparator: String = DEFAULT_CUSTOM_GROUPING_SEPARATOR,
    var capitalization: CapitalizationMode = DEFAULT_CAPITALIZATION,
    var prefix: String = DEFAULT_PREFIX,
    var suffix: String = DEFAULT_SUFFIX,
    var fixedLengthDecorator: FixedLengthDecorator = FixedLengthDecorator(),
    var arrayDecorator: ArrayDecorator = ArrayDecorator()
) : Scheme() {
    @get:Transient
    override val name = Bundle("integer.title")
    override val typeIcon = BASE_ICON

    override val decorators: List<SchemeDecorator>
        get() = listOf(fixedLengthDecorator, arrayDecorator)


    /**
     * Returns random formatted integers from [minValue] until [maxValue], inclusive.
     *
     * @param count the number of integers to generate
     * @return random formatted integers from [minValue] until [maxValue], inclusive
     */
    override fun generateUndecoratedStrings(count: Int) =
        List(count) { prefix + longToString(randomLong(minValue, maxValue)) + suffix }

    /**
     * Returns a random long in the range from [from] until [until], inclusive, without causing overflow.
     *
     * @param from inclusive lower bound
     * @param until inclusive upper bound
     * @return a random long in the range from [from] until [until], inclusive, without causing overflow
     */
    private fun randomLong(from: Long, until: Long) =
        if (from == Long.MIN_VALUE && until == Long.MAX_VALUE) random.nextLong()
        else if (until == Long.MAX_VALUE) random.nextLong(from - 1, until) + 1
        else random.nextLong(from, until + 1)

    /**
     * Returns a nicely formatted representation of [value].
     *
     * @param value the value to format
     * @return a nicely formatted representation of [value]
     */
    private fun longToString(value: Long): String {
        if (base != DECIMAL_BASE)
            return capitalization.transform(value.toString(base), random)

        val format = DecimalFormat()
        format.isGroupingUsed = groupingSeparator.isNotEmpty()
        format.minimumFractionDigits = 0
        format.maximumFractionDigits = 0
        format.decimalFormatSymbols = format.decimalFormatSymbols
            .also { it.groupingSeparator = groupingSeparator.getOrElse(0) { Char.MIN_VALUE } }

        return format.format(value)
    }


    override fun doValidate() =
        when {
            minValue > maxValue -> Bundle("integer.error.min_value_above_max")
            base !in MIN_BASE..MAX_BASE -> Bundle("integer.error.base_range", "$MIN_BASE..$MAX_BASE")
            groupingSeparator.length > 1 -> Bundle("integer.error.grouping_separator_length")
            else -> fixedLengthDecorator.doValidate() ?: arrayDecorator.doValidate()
        }

    override fun deepCopy(retainUuid: Boolean) =
        copy(
            fixedLengthDecorator = fixedLengthDecorator.deepCopy(retainUuid),
            arrayDecorator = arrayDecorator.deepCopy(retainUuid)
        ).also { if (retainUuid) it.uuid = this.uuid }


    /**
     * Holds constants.
     */
    companion object {
        /**
         * The base icon for integers.
         */
        val BASE_ICON = TypeIcon(RandomnessIcons.SCHEME, "123", listOf(Color(64, 182, 224, 154)))

        /**
         * The minimum value of the [base] field.
         */
        const val MIN_BASE = Character.MIN_RADIX

        /**
         * The maximum value of the [base] field.
         */
        const val MAX_BASE = Character.MAX_RADIX

        /**
         * The definition of decimal base.
         */
        const val DECIMAL_BASE = 10

        /**
         * The default value of the [minValue] field.
         */
        const val DEFAULT_MIN_VALUE = 0L

        /**
         * The default value of the [maxValue] field.
         */
        const val DEFAULT_MAX_VALUE = 1000L

        /**
         * The default value of the [base] field.
         */
        const val DEFAULT_BASE = DECIMAL_BASE

        /**
         * The default value of the [groupingSeparator] field.
         */
        const val DEFAULT_GROUPING_SEPARATOR = ""

        /**
         * The default value of the [customGroupingSeparator] field.
         */
        const val DEFAULT_CUSTOM_GROUPING_SEPARATOR = "/"

        /**
         * The default value of the [capitalization] field.
         */
        val DEFAULT_CAPITALIZATION = CapitalizationMode.LOWER

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
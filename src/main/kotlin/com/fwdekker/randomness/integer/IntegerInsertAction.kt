package com.fwdekker.randomness.integer

import com.fwdekker.randomness.DataInsertAction
import java.text.DecimalFormat
import kotlin.random.Random


/**
 * Generates a random integer based on the settings in [IntegerSettings].
 *
 * @param settings the settings to use for generating integers. Defaults to [IntegerSettings.default]
 */
class IntegerInsertAction(private val settings: IntegerSettings = IntegerSettings.default) : DataInsertAction() {
    override val name = "Insert Integer"


    /**
     * Returns a random integer between the minimum and maximum value, inclusive.
     *
     * @return a random integer between the minimum and maximum value, inclusive
     */
    override fun generateString() =
        convertToString(Random.nextLong(settings.minValue, settings.maxValue + 1))


    /**
     * Returns a nicely formatted representation of a long.
     *
     * @param integer a `long`
     * @return a nicely formatted representation of a long
     */
    private fun convertToString(integer: Long): String {
        if (settings.base != IntegerSettings.DECIMAL_BASE) {
            return integer.toString(settings.base)
        }


        val format = DecimalFormat()
        format.isGroupingUsed = settings.groupingSeparator != '\u0000'

        val symbols = format.decimalFormatSymbols
        symbols.groupingSeparator = settings.groupingSeparator
        format.minimumFractionDigits = 0
        format.maximumFractionDigits = 0
        format.decimalFormatSymbols = symbols

        return format.format(integer)
    }


    /**
     * Inserts an array of integers.
     */
    inner class ArrayAction : DataInsertAction.ArrayAction(this) {
        override val name = "Insert Integer Array"
    }
}
